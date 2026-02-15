package com.example.tokenization.service;

import com.example.tokenization.model.TokenMapping;
import com.example.tokenization.repository.TokenMappingRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class TokenizationService {
    private static final int TOKEN_LENGTH = 32;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[0-9A-Za-z]{32}$");

    private final TokenMappingRepository repository;
    private final TokenGenerator tokenGenerator;

    public TokenizationService(TokenMappingRepository repository, TokenGenerator tokenGenerator) {
        this.repository = repository;
        this.tokenGenerator = tokenGenerator;
    }

    public List<String> tokenize(List<String> accountNumbers) {
        if (accountNumbers == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body must be a JSON array of strings");
        }

        return accountNumbers.stream().map(this::tokenizeOne).toList();
    }

    public List<String> detokenize(List<String> tokens) {
        if (tokens == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body must be a JSON array of strings");
        }

        return tokens.stream()
                .map(this::detokenizeOne)
                .toList();
    }

    private String tokenizeOne(String accountNumber) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account number must not be blank");
        }

        return repository.findByAccountNumber(accountNumber)
                .map(TokenMapping::getToken)
                .orElseGet(() -> createNewMapping(accountNumber).getToken());
    }

    private String detokenizeOne(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token must not be blank");
        }
        // Validate token format before database lookup
        if (!TOKEN_PATTERN.matcher(token).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid token format. Expected 32 characters in [0-9A-Za-z]."
            );
        }

        // Perform lookup only after format validation
        TokenMapping mapping = repository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found"));

        return mapping.getAccountNumber();
    }

    private TokenMapping createNewMapping(String accountNumber) {
        String token = generateUniqueToken();
        TokenMapping mapping = new TokenMapping(token, accountNumber);
        return repository.save(mapping);
    }

    private String generateUniqueToken() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String token = tokenGenerator.generate(TOKEN_LENGTH);
            if (!repository.existsByToken(token)) {
                return token;
            }
        }
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate unique token");
    }

}
