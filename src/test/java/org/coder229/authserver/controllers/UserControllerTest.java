package org.coder229.authserver.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.coder229.authserver.model.RegisterRequest;
import org.coder229.authserver.model.RegisterResponse;
import org.coder229.authserver.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @Value("${authservice.secret}")
    public String SECRET;
    @Value("${authservice.salt}")
    public String SALT;

    @Test
    void register() throws Exception {
        String username = "username";
        String password = "p4ssWord$";
        RegisterRequest registerRequest = new RegisterRequest(username, password);

        MvcResult response = this.mockMvc.perform(
                        post("/api/v1/register")
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

        this.mockMvc.perform(
                        post("/api/v1/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}