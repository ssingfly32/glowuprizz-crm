package com.sanghee.glowuprizzcrm.publicform;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// publicform 프로세스가 떠 있는지 확인하는 헬스체크 엔드포인트. 인증이 필요 없다.
// 이 모듈은 admin 모듈을 의존할 수 없다(컴파일 타임 강제, docs/adr/0008 참고).
@RestController
public class PublicHealthController {

    @GetMapping("/public/health")
    public String health() {
        return "publicform module ok";
    }
}
