package com.sanghee.glowuprizzcrm;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.sanghee.glowuprizzcrm.admin.auth.LoginRequest;
import com.sanghee.glowuprizzcrm.admin.auth.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

// admin-bootstrap 전체 컨텍스트(AdminBootstrapApplication)를 실제 Postgres 컨테이너에
// 붙여서 띄운다. Security 필터 체인 + Flyway 마이그레이션 + JPA가 전부 실제로 동작하는
// 상태에서 검증한다 (라이브 curl 검증으로 잡았던 두 버그 모두 이 조합에서만 재현됐었다).
// @Transactional로 각 테스트 메서드가 끝나면 롤백되어, 시드 데이터(V2__seed_operator.sql)
// 외의 테스트 중 생성한 데이터는 서로 영향을 주지 않는다.
//
// 컨테이너를 @Testcontainers/@Container가 아니라 정적 초기화 블록으로 직접 시작한다
// ("싱글톤 컨테이너" 패턴). 이 추상 클래스를 상속하는 테스트 클래스가 여러 개일 때,
// @Testcontainers에게 맡기면 static 필드가 상속으로 공유되는데도 첫 번째 테스트 클래스가
// 끝나는 시점(@AfterAll)에 컨테이너를 stop시켜버려서, 두 번째 테스트 클래스부터는 이미 죽은
// 컨테이너에 연결을 시도하다 실패한다 (라이브 검증 중 실제로 재현된 문제).
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
public abstract class AbstractAdminIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    static {
        postgres.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected static final String SEED_OPERATOR_EMAIL = "operator@glowuprizz.com";
    protected static final String SEED_OPERATOR_PASSWORD = "glowup1234!";

    protected String obtainAccessToken() throws Exception {
        var request = new LoginRequest(SEED_OPERATOR_EMAIL, SEED_OPERATOR_PASSWORD);
        String body = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(body, LoginResponse.class).accessToken();
    }
}
