package com.sanghee.glowuprizzcrm.core.operator;

import com.sanghee.glowuprizzcrm.core.common.exception.ErrorCode;
import com.sanghee.glowuprizzcrm.core.common.validation.Assert;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "operators")
public class Operator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Operator() {
    }

    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    public Operator(String email, String passwordHash) {
        Assert.notBlank(email, ErrorCode.INVALID_EMAIL);
        Assert.matches(email, EMAIL_PATTERN, ErrorCode.INVALID_EMAIL);
        Assert.notBlank(passwordHash, ErrorCode.INVALID_PASSWORD_HASH);
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
