package com.sanghee.glowuprizzcrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// admin 모듈을 의존하지 않는다. 이 프로세스는 방문자의 요청을 처리하며 admin 인증/토큰
// 관련 코드에 컴파일 타임에도, 런타임(별도 프로세스/포트)에도 접근할 수 없다.
// 이 클래스의 패키지(com.sanghee.glowuprizzcrm)가 core/publicform 패키지의 공통 상위이므로,
// @SpringBootApplication 기본 컴포넌트/엔티티/리포지토리 스캔 범위에 둘 다 포함된다.
@SpringBootApplication
public class PublicBootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublicBootstrapApplication.class, args);
    }
}
