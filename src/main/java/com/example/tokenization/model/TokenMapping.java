package com.example.tokenization.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name="token_mapping",
indexes = {
    @Index(name = "idx_token_mapping_token", columnList = "token", unique = true),
    @Index(name = "idx_token_mapping_account", columnList = "accountNumber", unique = true)
})
public class TokenMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(nullable = false, unique = true, length = 128)
    private String accountNumber;

    @Column(nullable = false)
    private Instant createdAt;

    protected TokenMapping() {
        // JPA
    }

    public TokenMapping(String token, String accountNumber) {
        this.token = token;
        this.accountNumber = accountNumber;
    }

    @PrePersist
    public void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
