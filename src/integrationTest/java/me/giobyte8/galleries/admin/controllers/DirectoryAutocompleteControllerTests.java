package me.giobyte8.galleries.admin.controllers;

import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@WebAppConfiguration
class DirectoryAutocompleteControllerTests extends BaseIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private DirectoryRepository directoryRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = webAppContextSetup(context).build();
    }

    @Test
    void autocompleteShouldReturnEmptyFragmentForBlankQuery() throws Exception {
        mockMvc.perform(get("/admin/directories/autocomplete").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("list-group-item")
                )));
    }

    @Test
    void autocompleteShouldReturnAtMostFiveResults() throws Exception {
        for (int i = 0; i < 8; i++) {
            directoryRepository.save(Directory.builder()
                    .path("root/cat-" + i)
                    .build());
        }

        mockMvc.perform(get("/admin/directories/autocomplete").param("q", "cat"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        org.hamcrest.Matchers.containsString("root/cat-0")
                ))
                .andExpect(content().string(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString("root/cat-7")
                        )
                ));
    }
}

