package com.sanghee.glowuprizzcrm.core.common.exception;

public enum ErrorCode {
    INVALID_EMAIL("올바른 이메일 형식이 아닙니다."),
    INVALID_PASSWORD_HASH("비밀번호 해시는 비어 있을 수 없습니다."),
    INVALID_TEMPLATE_NAME("템플릿 이름은 1자 이상 255자 이하여야 합니다."),
    INVALID_TEMPLATE_CONTENT("HTML 내용은 비어 있을 수 없습니다."),
    INVALID_TEMPLATE_FILE_EXTENSION("HTML(.html) 파일만 등록할 수 있습니다."),
    INVALID_CAMPAIGN_NAME("캠페인 이름은 1자 이상 255자 이하여야 합니다."),
    INVALID_PUBLIC_SLUG("공개 URL 슬러그는 소문자, 숫자, 하이픈만 사용할 수 있습니다."),
    DUPLICATE_PUBLIC_SLUG("이미 사용 중인 공개 URL 슬러그입니다."),
    INVALID_LINK_TOKEN("배포 링크 토큰은 비어 있을 수 없습니다."),
    INVALID_VISITOR_TOKEN("방문자 토큰은 비어 있을 수 없습니다."),
    INVALID_SUBMISSION_DATA("신청 데이터는 비어 있을 수 없습니다."),
    AUTH_INVALID_CREDENTIALS("이메일 또는 비밀번호가 올바르지 않습니다."),
    TEMPLATE_NOT_FOUND("등록된 HTML 템플릿을 찾을 수 없습니다."),
    CAMPAIGN_NOT_FOUND("캠페인을 찾을 수 없습니다."),
    CAMPAIGN_NOT_PUBLISHED("공개되지 않은 캠페인입니다."),
    LINK_NOT_FOUND("배포 링크를 찾을 수 없습니다."),
    INTERNAL_ERROR("서버 내부 오류가 발생했습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
