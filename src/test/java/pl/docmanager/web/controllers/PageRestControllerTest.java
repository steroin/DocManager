package pl.docmanager.web.controllers;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.docmanager.dao.PageRepository;
import pl.docmanager.dao.PageSectionRepository;
import pl.docmanager.domain.PageBuilder;
import pl.docmanager.domain.SolutionBuilder;
import pl.docmanager.domain.UserBuilder;
import pl.docmanager.domain.page.Page;
import pl.docmanager.domain.solution.Solution;
import pl.docmanager.domain.user.User;
import pl.docmanager.web.controllers.validation.PageValidator;
import pl.docmanager.web.security.JwtTokenGenerator;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PageRestController.class)
public class PageRestControllerTest extends RestControllerTestBase {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PageRepository pageRepository;

    @MockBean
    private PageSectionRepository pageSectionRepository;

    @SpyBean
    private PageValidator pageValidator;

    @Before
    public void setup() {
        super.setup();

        Solution solution = new SolutionBuilder(1).build();
        User author = new UserBuilder(99, solution).build();
        Page page = new PageBuilder(1, solution)
                .withAutor(author)
                .withCreateDate(LocalDateTime.of(1970, 1, 1, 0, 0))
                .withName("examplePage")
                .withUrl("example_page").build();

        given(pageRepository.findBySolution_IdAndUrl(1, "example_page")).willReturn(Optional.of(page));

        Solution solution2 = new SolutionBuilder(2).build();
        User author2 = new UserBuilder(199, solution2).build();
        Page page2 = new PageBuilder(2, solution2)
                .withAutor(author2)
                .withCreateDate(LocalDateTime.of(1970, 1, 1, 0, 0))
                .withName("examplePage")
                .withUrl("example_page").build();

        given(pageRepository.findBySolution_IdAndUrl(2, "example_page")).willReturn(Optional.of(page2));
    }

    @Test
    public void getPageBySolutionIdAndUrlTestValid() throws Exception {
        String expectedJson = "{id: 1, solution: {id: 1}, name: 'examplePage', " +
                "createDate: '1970-01-01T00:00:00', " +
                "author: {'id': 99}, sections: [], state: 'ACTIVE', url: 'example_page'}";
        mvc.perform(get("/api/pages/solution/1/url/example_page")
                .contentType(MediaType.APPLICATION_JSON)
                .header("apiToken", validToken))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    public void getPageBySolutionIdAndUrlTestNoApiToken() throws Exception {
        mvc.perform(get("/api/pages/solution/1/url/example_page")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void getPageBySolutionIdAndUrlTestWrongApiToken() throws Exception {
        String invalidToken = JwtTokenGenerator.generateToken(USER_EMAIL, "invalidSecret", new Date(System.currentTimeMillis() + 1000000000));
        mvc.perform(get("/api/pages/solution/1/url/example_page")
                .contentType(MediaType.APPLICATION_JSON)
                .header("apiToken", invalidToken))
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void getPageBySolutionIdAndUrlTestPageNotFound() throws Exception {
        mvc.perform(get("/api/pages/solution/1/url/i_dont_exist")
                .contentType(MediaType.APPLICATION_JSON)
                .header("apiToken", validToken))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void getPageBySolutionIdAndUrlTestNoAccessToSolution() throws Exception {
        mvc.perform(get("/api/pages/solution/2/url/example_page")
                .contentType(MediaType.APPLICATION_JSON)
                .header("apiToken", validToken))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void addPageTestValid() throws Exception {
        mvc.perform(post("/api/pages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ " +
                        "   \"name\": \"page\", " +
                        "   \"solution\": { " +
                        "      \"id\": 1 " +
                        "   }, " +
                        "   \"author\": { " +
                        "       \"id\": 1" +
                        "   }," +
                        "   \"url\": \"url\"," +
                        "   \"sections\": [{ " +
                        "       \"name\": \"section\", " +
                        "       \"content\": \"sectionContent\", " +
                        "       \"index\": 0, " +
                        "       \"url\": \"sectionUrl\" " +
                        "   }]"+
                        " }")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .header("apiToken", validToken))
                .andDo(print())
                .andExpect(status().is(HttpStatus.OK.value()));
    }

    @Test
    public void addPageTestNullSolution() throws Exception {
        mvc.perform(post("/api/pages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ " +
                        "   \"name\": \"page\", " +
                        "   \"author\": { " +
                        "       \"id\": 1" +
                        "   }," +
                        "   \"url\": \"url\"," +
                        "   \"sections\": [{ " +
                        "       \"name\": \"section\", " +
                        "       \"content\": \"sectionContent\", " +
                        "       \"index\": 0, " +
                        "       \"url\": \"sectionUrl\" " +
                        "   }]"+
                        " }")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .header("apiToken", validToken))
                .andDo(print())
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void addPageTestNotMySolution() throws Exception {
        mvc.perform(post("/api/pages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ " +
                        "   \"name\": \"page\", " +
                        "   \"solution\": { " +
                        "      \"id\": 2 " +
                        "   }, " +
                        "   \"author\": { " +
                        "       \"id\": 1" +
                        "   }," +
                        "   \"url\": \"url\"," +
                        "   \"sections\": [{ " +
                        "       \"name\": \"section\", " +
                        "       \"content\": \"sectionContent\", " +
                        "       \"index\": 0, " +
                        "       \"url\": \"sectionUrl\" " +
                        "   }]"+
                        " }")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .header("apiToken", validToken))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void addPageTestNoApiToken() throws Exception {
        mvc.perform(post("/api/pages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ " +
                        "   \"name\": \"page\", " +
                        "   \"solution\": { " +
                        "      \"id\": 1 " +
                        "   }, " +
                        "   \"author\": { " +
                        "       \"id\": 1" +
                        "   }," +
                        "   \"url\": \"url\"," +
                        "   \"sections\": [{ " +
                        "       \"name\": \"section\", " +
                        "       \"content\": \"sectionContent\", " +
                        "       \"index\": 0, " +
                        "       \"url\": \"sectionUrl\" " +
                        "   }]"+
                        " }")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void addPageTestWrongApiToken() throws Exception {
        String invalidToken = JwtTokenGenerator.generateToken(USER_EMAIL, "invalidSecret", new Date(System.currentTimeMillis() + 1000000000));
        mvc.perform(post("/api/pages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ " +
                        "   \"name\": \"page\", " +
                        "   \"solution\": { " +
                        "      \"id\": 1 " +
                        "   }, " +
                        "   \"author\": { " +
                        "       \"id\": 1" +
                        "   }," +
                        "   \"url\": \"url\"," +
                        "   \"sections\": [{ " +
                        "       \"name\": \"section\", " +
                        "       \"content\": \"sectionContent\", " +
                        "       \"index\": 0, " +
                        "       \"url\": \"sectionUrl\" " +
                        "   }]"+
                        " }")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .header("apiToken", invalidToken))
                .andDo(print())
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    public void updatePageTestValid() throws Exception {
        mvc.perform(post("/api/pages/solution/1/url/example_page")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ " +
                    "   \"id\": 1, " +
                    "   \"name\": \"changedPageName\", " +
                    "   \"solution\": { " +
                    "      \"id\": 1 " +
                    "   }, " +
                    "   \"author\": { " +
                    "       \"id\": 99" +
                    "   }," +
                    "   \"url\": \"changedUrl\", " +
                    "   \"createDate\": \"1970-01-01T00:00:00\", " +
                    "   \"state\": \"ACTIVE\" " +
                    " }")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("apiToken", validToken))
            .andDo(print())
            .andExpect(status().is(HttpStatus.OK.value()));
        verify(pageRepository, times(1)).save(any());
    }

    @Test
    public void updatePageTestNonExistingPage() throws Exception {
        mvc.perform(post("/api/pages/solution/1/url/i_dont_exist")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{ " +
                    "   \"id\": 1, " +
                    "   \"name\": \"changedPageName\", " +
                    "   \"solution\": { " +
                    "      \"id\": 1 " +
                    "   }, " +
                    "   \"author\": { " +
                    "       \"id\": 99" +
                    "   }," +
                    "   \"url\": \"changedUrl\", " +
                    "   \"createDate\": \"1970-01-01T00:00:00\", " +
                    "   \"state\": \"ACTIVE\" " +
                    " }")
            .accept(MediaType.APPLICATION_JSON)
            .characterEncoding("UTF-8")
            .header("apiToken", validToken))
            .andDo(print())
            .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void updatePageTestNotMyPage() throws Exception {
        mvc.perform(post("/api/pages/solution/2/url/example_page")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ " +
                        "   \"id\": 2, " +
                        "   \"name\": \"changedPageName\", " +
                        "   \"solution\": { " +
                        "      \"id\": 2 " +
                        "   }, " +
                        "   \"author\": { " +
                        "       \"id\": 199" +
                        "   }," +
                        "   \"url\": \"changedUrl\", " +
                        "   \"createDate\": \"1970-01-01T00:00:00\", " +
                        "   \"state\": \"ACTIVE\" " +
                        " }")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .header("apiToken", validToken))
                .andDo(print())
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void updatePageTestNoApiToken() throws Exception {
        mvc.perform(post("/api/pages/solution/1/url/example_page")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ " +
                        "   \"id\": 1, " +
                        "   \"name\": \"changedPageName\", " +
                        "   \"solution\": { " +
                        "      \"id\": 1 " +
                        "   }, " +
                        "   \"author\": { " +
                        "       \"id\": 99" +
                        "   }," +
                        "   \"url\": \"changedUrl\", " +
                        "   \"createDate\": \"1970-01-01T00:00:00\", " +
                        "   \"state\": \"ACTIVE\" " +
                        " }")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }



    @Test
    public void updatePageTestWrongToken() throws Exception {
        String invalidToken = JwtTokenGenerator.generateToken(USER_EMAIL, "invalidSecret", new Date(System.currentTimeMillis() + 1000000000));
        mvc.perform(post("/api/pages/solution/1/url/example_page")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ " +
                        "   \"id\": 1, " +
                        "   \"name\": \"changedPageName\", " +
                        "   \"solution\": { " +
                        "      \"id\": 1 " +
                        "   }, " +
                        "   \"author\": { " +
                        "       \"id\": 99" +
                        "   }," +
                        "   \"url\": \"changedUrl\", " +
                        "   \"createDate\": \"1970-01-01T00:00:00\", " +
                        "   \"state\": \"ACTIVE\" " +
                        " }")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .header("apiToken", invalidToken))
                .andDo(print())
                .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }
}