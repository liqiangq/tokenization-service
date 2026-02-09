package com.example.tokenization.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomTokenGenerator implements TokenGenerator {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String generate(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive");
        }

        StringBuilder sb = new StringBuilder(length);
        for(int i = 0; i < length; i++) {
            int idx = secureRandom.nextInt(ALPHABET.length);
            sb.append(ALPHABET[idx]);
        }

        return sb.toString();
    }
}
