package com.sanghee.glowuprizzcrm.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sanghee.glowuprizzcrm.AbstractAdminIntegrationTest;
import com.sanghee.glowuprizzcrm.admin.auth.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

@DisplayName("POST /admin/auth/login 통합 테스트")
class AuthControllerIntegrationTest extends AbstractAdminIntegrationTest {

    @Test
    @DisplayName("올바른 이메일/비밀번호면 JWT를 발급한다")
    void login_returnsToken_whenCredentialsAreCorrect() throws Exception {
        LoginRequest request = new LoginRequest(SEED_OPERATOR_EMAIL, SEED_OPERATOR_PASSWORD);

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("비밀번호가 틀리면 401과 AUTH_INVALID_CREDENTIALS를 반환한다")
    void login_returns401_whenPasswordIsWrong() throws Exception {
        LoginRequest request = new LoginRequest(SEED_OPERATOR_EMAIL, "wrong-password");

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 401과 AUTH_INVALID_CREDENTIALS를 반환한다")
    void login_returns401_whenEmailNotFound() throws Exception {
        LoginRequest request = new LoginRequest("nobody@glowuprizz.com", SEED_OPERATOR_PASSWORD);

        mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_INVALID_CREDENTIALS"));
    }
}
