package demo.bookinfo.runtimes;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.micrometer.Label;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.PrometheusScrapingHandler;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.micrometer.backends.BackendRegistries;

import java.util.EnumSet;

public class Reviews extends AbstractVerticle {

  private static final int PORT = 9080;
  private static final Boolean RATINGS_ENABLED = Boolean.valueOf(System.getenv("ENABLE_RATINGS"));
  private static final String STAR_COLOR = System.getenv("STAR_COLOR") == null ? "black" : System.getenv("STAR_COLOR");
  private static final String SERVICES_DOMAIN = System.getenv("SERVICES_DOMAIN") == null ? "" : ("." + System.getenv("SERVICES_DOMAIN"));
  private static final String RATINGS_SERVICE = "http://ratings" + SERVICES_DOMAIN + ":9080/ratings";

  private final WebClient client;

  private Reviews(Vertx vertx) {
    client = WebClient.create(vertx);
  }

  public static void main(String[] args) {
    VertxOptions options = new VertxOptions().setMetricsOptions(new MicrometerMetricsOptions()
      .setPrometheusOptions(new VertxPrometheusOptions()
          .setPublishQuantiles(true)
          .setEnabled(true))
      .setLabels(EnumSet.of(Label.POOL_TYPE, Label.POOL_NAME, Label.CLASS_NAME, Label.HTTP_CODE, Label.HTTP_METHOD, Label.HTTP_PATH, Label.EB_ADDRESS, Label.EB_FAILURE, Label.EB_SIDE))
      .setEnabled(true));
    Vertx vertx = Vertx.vertx(options);
    vertx.deployVerticle(new Reviews(vertx));

    // Instrument JVM
    MeterRegistry registry = BackendRegistries.getDefaultNow();
    if (registry != null) {
      new ClassLoaderMetrics().bindTo(registry);
      new JvmMemoryMetrics().bindTo(registry);
      // new JvmGcMetrics().bindTo(registry);
      // new ProcessorMetrics().bindTo(registry);
      new JvmThreadMetrics().bindTo(registry);
    }
  }

  private String getJsonResponse (String productId, int starsReviewer1, int starsReviewer2) {
    String result = "{";
    result += "\"id\": \"" + productId + "\",";
    result += "\"reviews\": [";

    // reviewer 1:
    result += "{";
    result += "  \"reviewer\": \"Reviewer1\",";
    result += "  \"text\": \"An extremely entertaining play by Shakespeare. The slapstick humour is refreshing!\"";
    if (RATINGS_ENABLED) {
      if (starsReviewer1 != -1) {
        result += ", \"rating\": {\"stars\": " + starsReviewer1 + ", \"color\": \"" + STAR_COLOR + "\"}";
      }
      else {
        result += ", \"rating\": {\"error\": \"Ratings service is currently unavailable\"}";
      }
    }
    result += "},";

    // reviewer 2:
    result += "{";
    result += "  \"reviewer\": \"Reviewer2\",";
    result += "  \"text\": \"Absolutely fun and entertaining. The play lacks thematic depth when compared to other plays by Shakespeare.\"";
    if (RATINGS_ENABLED) {
      if (starsReviewer2 != -1) {
        result += ", \"rating\": {\"stars\": " + starsReviewer2 + ", \"color\": \"" + STAR_COLOR + "\"}";
      }
      else {
        result += ", \"rating\": {\"error\": \"Ratings service is currently unavailable\"}";
      }
    }
    result += "}";

    result += "]";
    result += "}";

    return result;
  }

  private void getRatings(String productId, String user, String useragent, String xreq, String xtraceid, String xspanid,
                                String xparentspanid, String xsampled, String xflags, String xotspan, Handler<JsonObject> handler) {
    HttpRequest<Buffer> request = client.get(RATINGS_SERVICE + "/" + productId);
    request.timeout(STAR_COLOR.equals("black") ? 10000 : 2500);
    if(xreq!=null) {
      request.putHeader("x-request-id",xreq);
    }
    if(xtraceid!=null) {
      request.putHeader("x-b3-traceid",xtraceid);
    }
    if(xspanid!=null) {
      request.putHeader("x-b3-spanid",xspanid);
    }
    if(xparentspanid!=null) {
      request.putHeader("x-b3-parentspanid",xparentspanid);
    }
    if(xsampled!=null) {
      request.putHeader("x-b3-sampled",xsampled);
    }
    if(xflags!=null) {
      request.putHeader("x-b3-flags",xflags);
    }
    if(xotspan!=null) {
      request.putHeader("x-ot-span-context",xotspan);
    }
    if(user!=null) {
      request.putHeader("end-user", user);
    }
    if(useragent!=null) {
      request.putHeader("user-agent", useragent);
    }
    request.send(ar -> {
      if (ar.succeeded()) {
        HttpResponse<Buffer> response = ar.result();
        int statusCode = response.statusCode();
        if (statusCode == 200) {
          handler.handle(response.bodyAsJsonObject());
        }else{
          System.out.println("Error: unable to contact "+ RATINGS_SERVICE +" got status of "+statusCode);
        }
      } else {
        ar.cause().printStackTrace();
      }
    });
  }

  @Override
  public void start() throws Exception {
    // Register stadium API
    HttpServerOptions serverOptions = new HttpServerOptions().setPort(PORT);
    Router router = Router.router(vertx);

    router.route(HttpMethod.GET, "/reviews/:productId").handler(this::getReview);
    router.get("/health").handler(ctx -> ctx.response().end());
    router.route("/metrics").handler(PrometheusScrapingHandler.create());
    vertx.createHttpServer().requestHandler(router)
        .listen(serverOptions.getPort(), serverOptions.getHost());
  }

  private void getReview(RoutingContext ctx) {
    String productId = ctx.request().getParam("productId");
    if (RATINGS_ENABLED) {
      getRatings(
          productId,
          ctx.request().getHeader("end-user"),
          ctx.request().getHeader("user-agent"),
          ctx.request().getHeader("x-request-id"),
          ctx.request().getHeader("x-b3-traceid"),
          ctx.request().getHeader("x-b3-spanid"),
          ctx.request().getHeader("x-b3-parentspanid"),
          ctx.request().getHeader("x-b3-sampled"),
          ctx.request().getHeader("x-b3-flags"),
          ctx.request().getHeader("x-ot-span-context"),
          ratingsResponse -> {
            int starsReviewer1 = -1;
            int starsReviewer2 = -1;
            if (ratingsResponse.containsKey("ratings")) {
              JsonObject ratings = ratingsResponse.getJsonObject("ratings");
              if (ratings.containsKey("Reviewer1")){
                starsReviewer1 = ratings.getInteger("Reviewer1");
              }
              if (ratings.containsKey("Reviewer2")){
                starsReviewer2 = ratings.getInteger("Reviewer2");
              }
            }
            String jsonResStr = getJsonResponse(productId, starsReviewer1, starsReviewer2);
            ctx.response().end(jsonResStr);
          });
    } else {
      String jsonResStr = getJsonResponse(productId, -1, -1);
      ctx.response().end(jsonResStr);
    }
  }
}
