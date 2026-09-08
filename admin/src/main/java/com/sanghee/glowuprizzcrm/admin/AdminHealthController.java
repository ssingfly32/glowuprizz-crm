package com.sanghee.glowuprizzcrm.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 스캐폴딩 단계의 임시 확인용 엔드포인트. 인증/캠페인/링크/성과 API로 교체 예정.
@RestController
public class AdminHealthController {

    @GetMapping("/admin/health")
    public String health() {
        return "admin module ok";
    }
}
