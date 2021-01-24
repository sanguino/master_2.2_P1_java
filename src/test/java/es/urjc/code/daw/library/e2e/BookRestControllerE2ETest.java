package es.urjc.code.daw.library.e2e;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookRestControllerE2ETest {

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost:"+port;
    }

    @Test
    public void getAllBooksTest() {

        //Given the DB initialized

        //When
        when().
            get("/api/books/").

        //Then
        then().
            statusCode(200).
            body("[0].title", equalTo("SUEÑOS DE ACERO Y NEON")).
            body("[1].title", equalTo("LA VIDA SECRETA DE LA MENTE"));

    }

    @Test
    public void addBookTest() {

        //Given
        Response response = given().
            auth().
            basic("user", "pass").
            contentType("application/json").
            body("{\"title\":\"book1\",\"description\":\"description1\" }").
            when().
            post("/api/books/").andReturn();

        int id = from(response.getBody().asString()).get("id");

        when()
                .get("/api/books/{id}",id).
                then()
                .statusCode(200);
    }

    @Test
    public void removeBookTest() {

        //Given
        given().
            auth().
            basic("admin", "pass").
        //When
        when().
            delete("/api/books/3").

        //Then
            then().
            statusCode(200);

        given().
        when()
            .get("/api/books/3").
            then()
            .statusCode(404);
    }
}
