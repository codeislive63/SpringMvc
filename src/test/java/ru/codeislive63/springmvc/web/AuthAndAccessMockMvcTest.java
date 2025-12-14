package ru.codeislive63.springmvc.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Функциональные тесты для требований главы 3:
 * - авторизация (успех/ошибка)
 * - разграничение доступа пассажир/администратор
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AuthAndAccessMockMvcTest {

    @Autowired
    MockMvc mockMvc;

    /**
     * Тест 1: Вход пассажира с корректными данными.
     */
    @Test
    void loginCustomer_success_redirectsToDashboard_andAuthenticated() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "customer@example.com")
                        .param("password", "cust123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(authenticated().withUsername("customer@example.com"));
    }

    /**
     * Тест 2: Вход с неверным паролем.
     */
    @Test
    void loginWrongPassword_shouldRedirectToLoginError_andUnauthenticated() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "customer@example.com")
                        .param("password", "WRONG"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/login?error"))
                .andExpect(unauthenticated());
    }

    /**
     * Тест 3: Доступ пассажира к административным разделам.
     */
    @Test
    @WithUserDetails("customer@example.com")
    void customerAccessAdmin_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin/panel/stations"))
                .andExpect(status().isForbidden());
    }

    /**
     * Тест 4: Доступ администратора к административной панели.
     */
    @Test
    @WithUserDetails("admin@example.com")
    void adminAccessAdminPanel_shouldBeOk() throws Exception {
        mockMvc.perform(get("/admin/panel/stations"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/stations/list"));
    }
}
