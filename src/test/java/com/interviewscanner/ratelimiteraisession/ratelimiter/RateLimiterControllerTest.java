package com.interviewscanner.ratelimiteraisession.ratelimiter;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimiterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsAllowedResponseForConfiguredEndpoint() throws Exception {
        mockMvc.perform(post("/api/rate-limit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":"client1","endpoint":"/search"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.remaining").value(999))
                .andExpect(jsonPath("$.retryAfterMs").value(0));
    }

    @Test
    void fallsBackToDefaultConfigForUnconfiguredEndpoint() throws Exception {
        mockMvc.perform(post("/api/rate-limit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":"client1","endpoint":"/unconfigured"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.allowed").value(true))
                .andExpect(jsonPath("$.remaining").value(99));
    }

    @Test
    void returns429WhenDenied() throws Exception {
        String body = """
                {"clientId":"denial-client","endpoint":"/denial-test"}
                """;
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(post("/api/rate-limit/check")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));
        }

        mockMvc.perform(post("/api/rate-limit/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.allowed").value(false))
                .andExpect(header().string("Retry-After", "1"));
    }
}
