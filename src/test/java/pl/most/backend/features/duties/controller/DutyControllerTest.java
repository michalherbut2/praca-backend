package pl.most.backend.features.duties.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.most.backend.features.duties.service.DutyService;
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

@WebMvcTest(controllers = DutyController.class)
class DutyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DutyService dutyService;

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

    @Test
    void shouldReturn200_WhenUserRequestsSlots() throws Exception {
        given(dutyService.getSlots(any(), any(), any(), any(), anyBoolean()))
                .willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/duties/slots")
                .param("category", "KITCHEN")
                .param("dateFrom", "2026-02-10")
                .param("dateTo", "2026-02-16")
                .with(authentication(createAuth(User.Role.USER))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200_WhenAdminGeneratesWeek() throws Exception {
        given(dutyService.generateLiturgyWeek(any()))
                .willReturn(Collections.emptyList());

        mockMvc.perform(post("/api/duties/admin/generate/liturgy")
                .param("startMonday", "2026-02-16")
                .contentType(MediaType.APPLICATION_JSON)
                .with(authentication(createAuth(User.Role.ADMIN)))
                .with(csrf()))
                .andExpect(status().isOk());
    }
}
