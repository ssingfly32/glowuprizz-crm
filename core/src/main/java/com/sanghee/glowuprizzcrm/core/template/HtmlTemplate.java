package com.sanghee.glowuprizzcrm.core.template;

import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.common.validation.Assert;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

// 운영자가 외부 AI 툴로 만들어 온 완성된 HTML을 그대로 저장한다.
// 이 시스템은 HTML을 생성하지 않고, 등록(붙여넣기/업로드)만 받는다.
@Entity
@Table(name = "html_templates")
public class HtmlTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected HtmlTemplate() {
    }

    public HtmlTemplate(Long operatorId, String name, String content) {
        Assert.notBlank(name, ErrorCode.INVALID_TEMPLATE_NAME);
        Assert.maxLength(name, 255, ErrorCode.INVALID_TEMPLATE_NAME);
        Assert.notBlank(content, ErrorCode.INVALID_TEMPLATE_CONTENT);
        this.operatorId = operatorId;
        this.name = name;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
