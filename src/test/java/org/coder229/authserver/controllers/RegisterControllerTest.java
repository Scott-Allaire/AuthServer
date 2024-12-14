package org.coder229.authserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.coder229.authserver.model.RegisterRequest;
import org.coder229.authserver.model.RegisterResponse;
import org.coder229.authserver.utilities.TestUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RegisterControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TestUtility testUtility;
    @Autowired
    private MockMvc mockMvc;

    @Nested
    class RegisterTests {
        @AfterEach
        void teardown() {
            testUtility.deleteAllUsers();
        }

        @Test
        void register() throws Exception {
            String username = "username";
            String password = "p4ssWord$";
            RegisterRequest registerRequest = new RegisterRequest(username, password);

            MvcResult response = mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk())
                    .andReturn();

            String body = response.getResponse().getContentAsString();
            RegisterResponse registerResponse = objectMapper.readValue(body, RegisterResponse.class);
            assertThat(registerResponse.username()).isEqualTo(registerRequest.username());
            assertThat(registerResponse.enabled()).isTrue();
            assertThat(registerResponse.verified()).isFalse();
        }

        @Test
        void duplicateUsername() throws Exception {
            String username = "username";
            String password = "p4ssWord$";
            RegisterRequest registerRequest = new RegisterRequest(username, password);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest());
        }


        @ParameterizedTest
        @ValueSource(strings = {
                "",
                "1234567",
                "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890a",
//            "p4ssWord",
//            "p4ssword$",
//            "passWord$",
//            "P4SSWORD$"
        })
        void registerPasswordRules(String password) throws Exception {
            String username = "username";
            RegisterRequest registerRequest = new RegisterRequest(username, password);

            mockMvc.perform(post("/api/v1/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

}