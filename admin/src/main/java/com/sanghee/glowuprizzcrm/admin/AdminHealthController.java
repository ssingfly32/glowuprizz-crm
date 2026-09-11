package com.sanghee.glowuprizzcrm.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// admin 프로세스가 떠 있는지 확인하는 헬스체크 엔드포인트. /admin/auth/login만
// permitAll이고 나머지는 SecurityConfig의 anyRequest().authenticated()에 걸리므로,
// 이 엔드포인트도 JWT가 있어야 응답한다.
@RestController
public class AdminHealthController {

    @GetMapping("/admin/health")
    public String health() {
        return "admin module ok";
    }
}
