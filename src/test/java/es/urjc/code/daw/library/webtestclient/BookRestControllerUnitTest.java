package es.urjc.code.daw.library.webtestclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class BookRestControllerUnitTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @Autowired
    ObjectMapper objectMapper;

    private WebTestClient webTestClient;

    @BeforeEach
    public void setup() {
        this.webTestClient = MockMvcWebTestClient
                .bindTo(mvc)
                .build();
    }

    @Test
    public void getAllBooksTest() {
        List<Book> books = Arrays.asList(
                new Book("book1", "desc1"),
                new Book("book2", "desc2")
        );

        when(bookService.findAll()).thenReturn(books);

        this.webTestClient.
            get()
            .uri("/api/books/")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$")
            .value(hasSize(2))
            .jsonPath("$[0].title")
            .value(equalTo("book1"))
            .jsonPath("$[1].title")
            .value(equalTo("book2"));
    }

    @Test
    @WithMockUser(username = "user", password = "pass", roles = "USER")
    public void addBookTest() {
        Book book = new Book("book1", "desc1");
        when(bookService.save(book)).thenReturn(book);

        this.webTestClient.
            post()
            .uri("/api/books/")
            .body(Mono.just(book), Book.class)
            .exchange()
            .expectStatus()
            .isCreated();
    }

    @Test
    @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
    public void removeBookTest() {
        doNothing().when(bookService).delete(1);

        this.webTestClient.
            delete()
            .uri("/api/books/1")
            .exchange()
            .expectStatus()
            .isOk();
    }


}


