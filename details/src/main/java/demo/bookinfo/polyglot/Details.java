package demo.bookinfo.runtimes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Details {

  @RequestMapping("/")
  public String home() {
    return "Hello Docker World";
  }

  @RequestMapping("/details/{id}")
  public BookDetails details(@PathVariable String id) {
    return new BookDetails(id, "William Shakespeare", 1595, "paperback", 200, "PublisherA", "English", "1234567890", "123-1234567890");
  }

  public static void main(String[] args) {
    SpringApplication.run(Details.class, args);
  }

}
