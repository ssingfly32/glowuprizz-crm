package com.sanghee.glowuprizzcrm.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// admin 프로세스가 떠 있는지 확인하는 헬스체크 엔드포인트. 인증이 필요 없다.
@RestController
public class AdminHealthController {

    @GetMapping("/admin/health")
    public String health() {
        return "admin module ok";
    }
}
