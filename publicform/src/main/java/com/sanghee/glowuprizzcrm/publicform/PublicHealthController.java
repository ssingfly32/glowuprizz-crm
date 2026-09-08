package com.sanghee.glowuprizzcrm.publicform;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// 스캐폴딩 단계의 임시 확인용 엔드포인트. /r/{token} 리다이렉트, /f/{slug} 폼 렌더링,
// 제출 API로 교체 예정. 이 모듈은 admin 모듈을 의존할 수 없다(컴파일 타임 강제).
@RestController
public class PublicHealthController {

    @GetMapping("/public/health")
    public String health() {
        return "publicform module ok";
    }
}
