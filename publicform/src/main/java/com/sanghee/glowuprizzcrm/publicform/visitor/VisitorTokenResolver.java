package com.sanghee.glowuprizzcrm.publicform.visitor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;

// 방문자 식별용 익명 쿠키를 다룬다. 있으면 재사용, 없으면 발급한다 (docs/adr/0002).
@Component
public class VisitorTokenResolver {

    public static final String COOKIE_NAME = "visitor_token";
    private static final int ONE_YEAR_SECONDS = 60 * 60 * 24 * 365;

    public String resolve(HttpServletRequest request, HttpServletResponse response) {
        String existing = findExistingToken(request);
        if (existing != null) {
            return existing;
        }
        String token = UUID.randomUUID().toString();
        issueCookie(response, token);
        return token;
    }

    private String findExistingToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void issueCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ONE_YEAR_SECONDS);
        response.addCookie(cookie);
    }
}
