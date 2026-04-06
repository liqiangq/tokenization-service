package com.example.tokenization;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
public class TokenizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void tokenizeAndDetokenize_roundTrip() throws Exception {
        List<String> accounts = List.of(
                "4111-1111-1111-1111",
                "4444-3333-2222-1111",
                "4444-1111-2222-3333"
        );

        MvcResult tokenizeResult = mockMvc.perform(
                        post("/tokenize")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(accounts))
                )
                .andExpect(status().isOk())
                .andReturn();

        List<String> tokens = objectMapper.readValue(
                tokenizeResult.getResponse().getContentAsString(),
                new TypeReference<List<String>>() {}
        );

        assertThat(tokens).hasSize(accounts.size());
        assertThat(new HashSet<>(tokens)).hasSize(accounts.size()); // tokens should be unique

        MvcResult detokenizeResult = mockMvc.perform(
                        post("/detokenize")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tokens))
                )
                .andExpect(status().isOk())
                .andReturn();

        List<String> restored = objectMapper.readValue(
                detokenizeResult.getResponse().getContentAsString(),
                new TypeReference<List<String>>() {}
        );

        assertThat(restored).containsExactlyElementsOf(accounts);
    }

    @Test
    void tokenize_shouldBeIdempotent_forSameAccount() throws Exception {
        List<String> accounts = List.of("4111-1111-1111-1111");

        String body = objectMapper.writeValueAsString(accounts);

        MvcResult r1 = mockMvc.perform(post("/tokenize").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult r2 = mockMvc.perform(post("/tokenize").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();

        List<String> t1 = objectMapper.readValue(r1.getResponse().getContentAsString(), new TypeReference<List<String>>() {});
        List<String> t2 = objectMapper.readValue(r2.getResponse().getContentAsString(), new TypeReference<List<String>>() {});

        assertThat(t1).hasSize(1);
        assertThat(t2).hasSize(1);
        assertThat(t2.get(0)).isEqualTo(t1.get(0));
    }

    @Test
    void detokenize_invalidTokenFormat_shouldReturn400() throws Exception {
        List<String> tokens = List.of("unknown-token-123"); // invalid (wrong length/charset)
        mockMvc.perform(
                        post("/detokenize")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tokens))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void detokenize_unknownToken_shouldReturn404() throws Exception {
        String validButUnknown = "A".repeat(32);
        List<String> tokens = List.of(validButUnknown);

        mockMvc.perform(
                        post("/detokenize")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(tokens))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void tokenize_blankAccount_shouldReturnStructured400() throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/tokenize")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of("   ")))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        Map<String, Object> error = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

        assertThat(error.get("status")).isEqualTo(400);
        assertThat(error.get("message")).isEqualTo("Account number must not be blank");
        assertThat(error.get("path")).isEqualTo("/tokenize");
    }

    @Test
    void detokenize_emptyArray_shouldReturn400() throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/detokenize")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(List.of()))
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        Map<String, Object> error = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {});

        assertThat(error.get("message")).isEqualTo("Request body must be a JSON array of tokens");
    }

}
