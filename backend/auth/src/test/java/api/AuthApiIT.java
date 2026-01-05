package api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthApiIT {

  @LocalServerPort int port;

  @Test
  void identify_should_accept_email() {
    given()
        .port(port)
        .contentType(ContentType.JSON)
        .body(Map.of("identity", "alice@example.com"))
        .when()
        .post("/auth/identify-and-send-otp")
        .then()
        .statusCode(200)
        .body("identityType", equalTo("EMAIL"));
  }
}
