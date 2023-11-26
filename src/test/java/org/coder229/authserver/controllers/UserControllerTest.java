package org.coder229.authserver.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.coder229.authserver.model.*;
import org.coder229.authserver.persistence.Token;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.persistence.UserRepository;
import org.coder229.authserver.utilities.TestUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    private TestUtility testUtility;
    @Autowired
    private MockMvc mockMvc;
    @Value("${authservice.secret}")
    public String SECRET;
    @Value("${authservice.salt}")
    public String SALT;

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

    @Nested
    class UserListTests {
        @AfterEach
        void teardown() {
            testUtility.deleteAllUsers();
        }

        @Test
        void adminCanGetUsers() throws Exception {
            int numberOfUsers = 5;
            User admin = testUtility.createUser("admin", "password", List.of(UserRole.ADMIN));
            Token token = testUtility.createToken(admin, TokenType.ACCESS, Instant.now().plusSeconds(60));
            List<User> users = IntStream.range(1, numberOfUsers)
                    .mapToObj(num -> testUtility.createUser("user" + num, "password", List.of(UserRole.USER)))
                    .sorted(Comparator.comparing(User::getUsername))
                    .toList();
            List<String> expected = users.stream().map(User::getUsername).toList();

            MvcResult response = mockMvc.perform(get("/api/v1/users?page=0&size=5&sort=username")
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getValue()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();

            TypeReference<PageResponse<UserResponse>> typeRef = new TypeReference<>() {
            };
            String body = response.getResponse().getContentAsString();
            PageResponse<UserResponse> page = objectMapper.readValue(body, typeRef);

            assertThat(page).isNotNull();
            assertThat(page.pageNum()).isEqualTo(0);
            assertThat(page.pageSize()).isEqualTo(5);
            assertThat(page.totalPages()).isGreaterThanOrEqualTo(1);
            assertThat(page.totalItems()).isGreaterThanOrEqualTo(numberOfUsers);
            List<String> usernames = page.items().stream().map(UserResponse::username).toList();
            assertThat(usernames).isNotEmpty();
            assertThat(usernames).containsAll(expected);
        }

        @Test
        void userCantGetUsers() throws Exception {
            User admin = testUtility.createUser("user", "password", List.of(UserRole.USER));
            Token token = testUtility.createToken(admin, TokenType.ACCESS, Instant.now().plusSeconds(60));

            mockMvc.perform(get("/api/v1/users")
                            .accept(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getValue()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}