package pl.most.backend.features.scheduler.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.most.backend.features.scheduler.service.ServiceSchedulerService;
import pl.most.backend.model.entity.User;
import pl.most.backend.security.AppUserDetails;
import pl.most.backend.security.AppUserDetailsService;
import pl.most.backend.security.JwtAuthenticationFilter;
import pl.most.backend.security.JwtTokenProvider;
import pl.most.backend.security.UserDetailsServiceImpl;
import pl.most.backend.service.GoogleCalendarService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test warstwy HTTP kontrolera {@link ServiceSchedulerController}.
 * Uzywa @WebMvcTest (laduje TYLKO warstwe kontrolera) z mockami serwisow.
 */
@WebMvcTest(controllers = ServiceSchedulerController.class)
class ServiceSchedulerControllerTest {

        @Autowired
        private MockMvc mockMvc;

        // --- Mockowane beany ---

        @MockitoBean
        private ServiceSchedulerService serviceSchedulerService;

        @MockitoBean
        private GoogleCalendarService googleCalendarService;

        @MockitoBean
        private JwtTokenProvider jwtTokenProvider;

        @MockitoBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockitoBean
        private AppUserDetailsService appUserDetailsService;

        @MockitoBean
        private UserDetailsServiceImpl userDetailsService;

        // --- Helper: tworzy authentication z AppUserDetails ---

        private static UsernamePasswordAuthenticationToken createAuth(User.Role role) {
                User user = new User();
                user.setId(UUID.randomUUID());
                user.setEmail("test@example.com");
                user.setPassword("password");
                user.setFirstName("Test");
                user.setLastName("User");
                user.setRole(role);
                user.setIsActive(true);

                AppUserDetails principal = new AppUserDetails(user);
                return new UsernamePasswordAuthenticationToken(
                                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        }

        // --- USER ENDPOINT: GET /api/scheduler/slots ---

        @Test
        void shouldReturn200_WhenUserRequestsSlots() throws Exception {
                // Given
                given(serviceSchedulerService.getSlots(any(), any(), any(), any(), anyBoolean()))
                                .willReturn(Collections.emptyList());

                // When & Then
                mockMvc.perform(get("/api/scheduler/slots")
                                .param("category", "KITCHEN")
                                .param("dateFrom", "2026-02-10")
                                .param("dateTo", "2026-02-16")
                                .with(authentication(createAuth(User.Role.USER))))
                                .andExpect(status().isOk());
        }

        // --- ADMIN ENDPOINT: POST /api/scheduler/admin/generate/liturgy ---

        @Test
        void shouldReturn200_WhenAdminGeneratesWeek() throws Exception {
                // Given
                given(serviceSchedulerService.generateLiturgyWeek(any()))
                                .willReturn(Collections.emptyList());

                // When & Then
                mockMvc.perform(post("/api/scheduler/admin/generate/liturgy")
                                .param("startMonday", "2026-02-16")
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(authentication(createAuth(User.Role.ADMIN)))
                                .with(csrf()))
                                .andExpect(status().isOk());
        }
}