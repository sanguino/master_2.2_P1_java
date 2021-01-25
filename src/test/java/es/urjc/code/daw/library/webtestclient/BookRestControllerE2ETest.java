package es.urjc.code.daw.library.webtestclient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

import static io.restassured.path.json.JsonPath.from;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class BookRestControllerE2ETest {

    private WebTestClient webTestClient;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setup() throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        HttpClient httpClient = HttpClient.create()
                .secure(sslSpec -> sslSpec.sslContext(sslContext))
                .baseUrl("https://localhost:" + port);
        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        this.webTestClient = WebTestClient
                .bindToServer(connector)
                .build();
    }

    @Test
    public void getAllBooksTest() {
        this.webTestClient
                .get()
                .uri("/api/books/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$[0].title")
                .value(equalTo("SUEÑOS DE ACERO Y NEON"))
                .jsonPath("$[1].title")
                .value(equalTo("LA VIDA SECRETA DE LA MENTE"));
    }

    @Test
    public void addBookTest() {
        String authHeader = "Basic " + Base64Utils.encodeToString(("user:pass").getBytes(UTF_8));
        String bookToCreate = "{\"title\":\"book1\",\"description\":\"description1\" }";

        FluxExchangeResult<String> result = this.webTestClient
            .post()
            .uri("/api/books/")
            .header("Authorization", authHeader)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(BodyInserters.fromValue(bookToCreate))
            .exchange()
            .expectStatus()
            .isCreated()
            .returnResult(String.class);

        int id = from(result.getResponseBody().blockFirst()).get("id");

        this.webTestClient
                .get()
                .uri("/api/books/" + id)
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void removeBookTest() {
        String authHeader = "Basic " + Base64Utils.encodeToString(("admin:pass").getBytes(UTF_8));
        this.webTestClient
                .delete()
                .uri("/api/books/3")
                .header("Authorization", authHeader)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus()
                .isOk();

        this.webTestClient
                .get()
                .uri("/api/books/3")
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
