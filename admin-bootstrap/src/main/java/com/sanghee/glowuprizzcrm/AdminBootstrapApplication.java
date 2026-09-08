package com.sanghee.glowuprizzcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// publicform 모듈을 의존하지 않는다 (별도 포트/프로세스로 떠서 admin 인증 토큰과
// 실제로 다른 origin이 되게 하는 것이 목적 — docs/adr/0011 참고).
// 이 클래스의 패키지(com.sanghee.glowuprizzcrm)가 core/admin 패키지의 공통 상위이므로,
// @SpringBootApplication 기본 컴포넌트/엔티티/리포지토리 스캔 범위에 둘 다 포함된다.
@SpringBootApplication
public class AdminBootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminBootstrapApplication.class, args);
    }
}
