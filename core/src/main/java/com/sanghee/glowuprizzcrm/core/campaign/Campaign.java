package com.sanghee.glowuprizzcrm.core.campaign;

import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.common.validation.Assert;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

// 캠페인 = 커스텀 신청 폼 인스턴스 (별도 CustomForm 엔티티로 나누지 않는다).
// 공개 여부(published)로 /r/{token}, /f/{slug} 접근 가능 여부를 제어한다.
@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false)
    private Long htmlTemplateId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String publicSlug;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Campaign() {
    }

    private static final String PUBLIC_SLUG_PATTERN = "^[a-z0-9]+(-[a-z0-9]+)*$";

    public Campaign(Long operatorId, Long htmlTemplateId, String name, String publicSlug) {
        Assert.notBlank(name, ErrorCode.INVALID_CAMPAIGN_NAME);
        Assert.maxLength(name, 255, ErrorCode.INVALID_CAMPAIGN_NAME);
        Assert.notBlank(publicSlug, ErrorCode.INVALID_PUBLIC_SLUG);
        Assert.matches(publicSlug, PUBLIC_SLUG_PATTERN, ErrorCode.INVALID_PUBLIC_SLUG);
        this.operatorId = operatorId;
        this.htmlTemplateId = htmlTemplateId;
        this.name = name;
        this.publicSlug = publicSlug;
        this.published = false;
        this.createdAt = Instant.now();
    }

    public void publish() {
        this.published = true;
    }

    public void unpublish() {
        this.published = false;
    }

    public Long getId() {
        return id;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public Long getHtmlTemplateId() {
        return htmlTemplateId;
    }

    public String getName() {
        return name;
    }

    public String getPublicSlug() {
        return publicSlug;
    }

    public boolean isPublished() {
        return published;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
