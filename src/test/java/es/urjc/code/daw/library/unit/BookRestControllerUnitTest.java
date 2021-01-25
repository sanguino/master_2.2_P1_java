package es.urjc.code.daw.library.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.code.daw.library.book.Book;
import es.urjc.code.daw.library.book.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookRestControllerUnitTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void getAllBooksTest() throws Exception {
        List<Book> books = Arrays.asList(
            new Book("book1", "desc1"),
            new Book("book2", "desc2")
        );
        when(bookService.findAll()).thenReturn(books);
        mvc.perform(get("/api/books/")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].title", equalTo("book1")))
            .andExpect(jsonPath("$[1].title", equalTo("book2")));
    }

    @Test
    @WithMockUser(username = "user", password = "pass", roles = "USER")
    public void addBookTest() throws Exception {
        Book book = new Book("book1", "description1");
        book.setId(1);
        when(bookService.save(any(Book.class))).thenReturn(book);
        mvc.perform(post("/api/books/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", equalTo(1)))
            .andExpect(jsonPath("$.title", equalTo("book1")))
            .andExpect(jsonPath("$.description", equalTo("description1")));
    }

    @Test
    @WithMockUser(username = "user", password = "pass", roles = "ADMIN")
    public void removeBookTest() throws Exception {
        doNothing().when(bookService).delete(1);
        mvc.perform(delete("/api/books/1"))
            .andExpect(status().isOk());
    }
}


