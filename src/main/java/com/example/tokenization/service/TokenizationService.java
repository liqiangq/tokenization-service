package com.example.tokenization.service;

import com.example.tokenization.model.TokenMapping;
import com.example.tokenization.repository.TokenMappingRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class TokenizationService {
    private static final int TOKEN_LENGTH = 32;
    private static final int MAX_GENERATION_ATTEMPTS = 10;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[0-9A-Za-z]{32}$");

    private final TokenMappingRepository repository;
    private final TokenGenerator tokenGenerator;

    public TokenizationService(TokenMappingRepository repository, TokenGenerator tokenGenerator) {
        this.repository = repository;
        this.tokenGenerator = tokenGenerator;
    }

    public List<String> tokenize(List<String> accountNumbers) {
        return requireBody(accountNumbers, "Request body must be a JSON array of account numbers")
                .stream()
                .map(this::tokenizeOne)
                .toList();
    }

    public List<String> detokenize(List<String> tokens) {
        return requireBody(tokens, "Request body must be a JSON array of tokens")
                .stream()
                .map(this::detokenizeOne)
                .toList();
    }

    private String tokenizeOne(String accountNumber) {
        String normalizedAccountNumber = normalize(accountNumber, "Account number must not be blank");

        return repository.findByAccountNumber(normalizedAccountNumber)
                .map(TokenMapping::getToken)
                .orElseGet(() -> createNewMapping(normalizedAccountNumber).getToken());
    }

    private String detokenizeOne(String token) {
        String normalizedToken = normalize(token, "Token must not be blank");

        if (!TOKEN_PATTERN.matcher(normalizedToken).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid token format. Expected 32 characters in [0-9A-Za-z]."
            );
        }

        TokenMapping mapping = repository.findByToken(normalizedToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));

        return mapping.getAccountNumber();
    }

    private TokenMapping createNewMapping(String accountNumber) {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String token = tokenGenerator.generate(TOKEN_LENGTH);

            if (repository.existsByToken(token)) {
                continue;
            }

            try {
                return repository.saveAndFlush(new TokenMapping(token, accountNumber));
            } catch (DataIntegrityViolationException exception) {
                TokenMapping existingMapping = repository.findByAccountNumber(accountNumber).orElse(null);
                if (existingMapping != null) {
                    return existingMapping;
                }
            }
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate unique token");
    }

    private <T> List<T> requireBody(List<T> values, String message) {
        if (values == null || values.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return values;
    }

    private String normalize(String value, String message) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        String normalizedValue = value.trim();
        if (normalizedValue.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return normalizedValue;
    }
}
