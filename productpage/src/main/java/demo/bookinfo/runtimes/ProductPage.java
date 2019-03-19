package demo.bookinfo.runtimes;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author Joel Takvorian
 */
@Path("/")
@RequestScoped
public class ProductPage {

    private static final String SERVICES_DOMAIN = System.getenv("SERVICES_DOMAIN") == null ? "" : ("." + System.getenv("SERVICES_DOMAIN"));

    @GET
    @Path("/products")
    @Produces("application/json")
    public Response productsRoute() throws Exception {
        return Response.ok()
                .entity(getProducts())
                .build();
    }

    @GET
    @Path("/products/{productId}")
    @Produces("application/json")
    public Response productsRoute(@PathParam("productId") String productId) throws Exception {
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target("http://details" + SERVICES_DOMAIN + ":8080/details/" + productId);
        Response response = target.request().get();
        int code = response.getStatus();
        if (code == 200) {
            return Response.ok()
                .entity(response.getEntity())
                .build();
        }
        return Response.status(code)
            .entity(new Error("Sorry, product details are currently unavailable for this book."))
            .build();
    }

    @GET
    @Path("/products/{productId}/reviews")
    @Produces("application/json")
    public Response reviewsRoute(@PathParam("productId") String productId) throws Exception {
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target("http://reviews" + SERVICES_DOMAIN + ":9080/reviews/" + productId);
        Response response = target.request().get();
        int code = response.getStatus();
        if (code == 200) {
            return Response.ok()
                .entity(response.getEntity())
                .build();
        }
        return Response.status(code)
            .entity(new Error("Sorry, product reviews are currently unavailable for this book."))
            .build();
    }

    @GET
    @Path("/products/{productId}/ratings")
    @Produces("application/json")
    public Response ratingsRoute(@PathParam("productId") String productId) throws Exception {
        Client client = ClientBuilder.newBuilder().build();
        WebTarget target = client.target("http://ratings" + SERVICES_DOMAIN + ":9080/ratings/" + productId);
        Response response = target.request().get();
        int code = response.getStatus();
        if (code == 200) {
            return Response.ok()
                .entity(response.getEntity())
                .build();
        }
        return Response.status(code)
            .entity(new Error("Sorry, product ratings are currently unavailable for this book."))
            .build();
    }

    private static List<Product> getProducts() {
        return Arrays.asList(new Product(0, "The Comedy of Errors",
            "<a href=\"https://en.wikipedia.org/wiki/The_Comedy_of_Errors\">Wikipedia Summary</a>: The Comedy of Errors is one of <b>William Shakespeare\\'s</b> early plays. It is his shortest and one of his most farcical comedies, with a major part of the humour coming from slapstick and mistaken identity, in addition to puns and word play."));
    }

    private static Optional<Product> getProduct(String productId) {
        List<Product> products = getProducts();
        int id = Integer.parseInt(productId);
        if (id >= products.size()) {
            return Optional.empty();
        }
        return Optional.of(products.get(id));
    }

    static class Product {
        int id;
        String title;
        String descriptionHtml;

        Product(int id, String title, String descriptionHtml) {
            this.id = id;
            this.title = title;
            this.descriptionHtml = descriptionHtml;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getDescriptionHtml() {
            return descriptionHtml;
        }
    }

    static class Error {
        String error;

        Error(String error) {
            this.error = error;
        }

        public String getError() {
            return this.error;
        }
    }
}
