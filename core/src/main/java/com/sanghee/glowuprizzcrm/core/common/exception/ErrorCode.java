package com.sanghee.glowuprizzcrm.core.common.exception;

public enum ErrorCode {
    INVALID_EMAIL("올바른 이메일 형식이 아닙니다."),
    INVALID_PASSWORD_HASH("비밀번호 해시는 비어 있을 수 없습니다."),
    INVALID_TEMPLATE_NAME("템플릿 이름은 1자 이상 255자 이하여야 합니다."),
    INVALID_TEMPLATE_CONTENT("HTML 내용은 비어 있을 수 없습니다."),
    INVALID_CAMPAIGN_NAME("캠페인 이름은 1자 이상 255자 이하여야 합니다."),
    INVALID_PUBLIC_SLUG("공개 URL 슬러그는 소문자, 숫자, 하이픈만 사용할 수 있습니다."),
    INVALID_LINK_TOKEN("배포 링크 토큰은 비어 있을 수 없습니다."),
    INVALID_VISITOR_TOKEN("방문자 토큰은 비어 있을 수 없습니다."),
    INVALID_SUBMISSION_DATA("신청 데이터는 비어 있을 수 없습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
