package com.se4801.clearance.controller;

import com.se4801.clearance.config.OpenApiConfig;
import com.se4801.clearance.dto.response.PageResponse;
import com.se4801.clearance.service.AdminUserService;
import com.se4801.clearance.service.AuthService;
import com.se4801.clearance.service.OfficeReviewService;
import com.se4801.clearance.service.PasswordResetService;
import com.se4801.clearance.service.RegistrarClearanceService;
import com.se4801.clearance.security.CustomUserDetailsService;
import com.se4801.clearance.security.JwtService;
import com.se4801.clearance.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        AuthController.class,
        AdminUserController.class,
        OfficeReviewController.class,
        RegistrarClearanceController.class
})
@Import({SecurityAccessTest.TestSecurityConfig.class, OpenApiConfig.class})
class SecurityAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private PasswordResetService passwordResetService;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private OfficeReviewService officeReviewService;

    @MockBean
    private RegistrarClearanceService registrarClearanceService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void unauthenticatedRequestToMeReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCannotAccessAdminUserCreation() throws Exception {
        mockMvc.perform(post("/api/admin/users")
                        .contentType("application/json")
                        .content("""
                                {
                                  "fullName": "Office Staff",
                                  "email": "staff@test.com",
                                  "password": "password123",
                                  "role": "OFFICE_STAFF",
                                  "officeId": 1
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentCannotAccessOfficeSteps() throws Exception {
        mockMvc.perform(get("/api/office/clearance-steps"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OFFICE_STAFF")
    void officeStaffCannotAccessRegistrarRequests() throws Exception {
        mockMvc.perform(get("/api/registrar/clearance-requests"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "REGISTRAR")
    void registrarCanAccessRegistrarRequests() throws Exception {
        when(registrarClearanceService.listRequestsForRegistrar(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(new PageResponse<>(List.of(), 0, 10, 0, 0, true));

        mockMvc.perform(get("/api/registrar/clearance-requests"))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint((request, response, authException) ->
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                            .accessDeniedHandler((request, response, accessDeniedException) ->
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN)))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }
}
