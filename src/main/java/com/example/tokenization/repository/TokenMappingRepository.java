package com.example.tokenization.repository;

import com.example.tokenization.model.TokenMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenMappingRepository extends JpaRepository<TokenMapping, Long> {

    Optional<TokenMapping> findByAccountNumber(String accountNumber);
    Optional<TokenMapping> findByToken(String token);
    boolean existsByToken(String token);
}
