package me.giobyte8.galleries.admin.security;

import me.giobyte8.galleries.persistence.models.User;
import me.giobyte8.galleries.persistence.repositories.UserRepository;
import me.giobyte8.galleries.scanner.BaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
class AdminSecurityIntegrationTests extends BaseIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setupUsers() {
        mockMvc = webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .username("dev_admin")
                .password(passwordEncoder.encode("password"))
                .roles(List.of("ROLE_ADMIN"))
                .enabled(true)
                .build());

        userRepository.save(User.builder()
                .id(UUID.randomUUID())
                .username("viewer")
                .password(passwordEncoder.encode("password"))
                .roles(List.of("ROLE_VIEWER"))
                .enabled(true)
                .build());
    }

    @Test
    void anonymousRequestToAdminShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/galleries"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void adminLoginShouldRedirectToGalleries() throws Exception {
        mockMvc.perform(formLogin("/admin/login")
                        .user("username", "dev_admin")
                        .password("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/galleries"));
    }

    @Test
    void nonAdminUserShouldRedirectToForbiddenLoginError() throws Exception {
        var loginResult = mockMvc.perform(formLogin("/admin/login")
                        .user("username", "viewer")
                        .password("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        var session = loginResult.getRequest().getSession(false);

        assert session != null;
        mockMvc.perform(get("/admin/galleries")
                        .session((org.springframework.mock.web.MockHttpSession) session)
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login?error=forbidden"));
    }

    @Test
    void logoutShouldInvalidateSession() throws Exception {
        mockMvc.perform(post("/admin/logout")
                        .with(user("dev_admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login?logout"));

        mockMvc.perform(get("/admin/galleries"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void htmxPostShouldRequireCsrfToken() throws Exception {
        mockMvc.perform(post("/admin/fragments/directories/{id}/trigger-scan", UUID.randomUUID())
                        .header("HX-Request", "true")
                        .with(user("dev_admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login?error=forbidden"));

        mockMvc.perform(post("/admin/fragments/directories/{id}/trigger-scan", UUID.randomUUID())
                        .header("HX-Request", "true")
                        .with(user("dev_admin").roles("ADMIN"))
                        .with(csrf())
                        )
                .andExpect(status().isOk());
    }

    @Test
    void apiShouldRemainAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk());
    }
}




