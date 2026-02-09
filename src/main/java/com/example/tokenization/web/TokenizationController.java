package com.example.tokenization.web;

import com.example.tokenization.service.TokenizationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TokenizationController {

    private final TokenizationService tokenizationService;

    public TokenizationController(TokenizationService tokenizationService) {
        this.tokenizationService = tokenizationService;
    }

    @PostMapping("/tokenize")
    public List<String> tokenize(@RequestBody List<String> accountNumbers) {
        return tokenizationService.tokenize(accountNumbers);
    }

    @PostMapping("/detokenize")
    public List<String> detokenize(@RequestBody List<String> tokens) {
        return tokenizationService.detokenize(tokens);
    }
}
