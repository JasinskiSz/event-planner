package com.szymon_jasinski.eventplanner.integrationtests.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.szymon_jasinski.eventplanner.EventPlannerApplication;
import com.szymon_jasinski.eventplanner.dtos.RegisterDTO;
import com.szymon_jasinski.eventplanner.entities.User;
import com.szymon_jasinski.eventplanner.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = EventPlannerApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static Stream<Arguments> provideIncorrectEmails() {
        return Stream.of(
                Arguments.of("abc"),
                Arguments.of("abc@"),
                Arguments.of("abc@pl"),
                Arguments.of("abc@pl."),
                Arguments.of("@gmail.com"),
                Arguments.of("asd@abc@gmail"),
                Arguments.of("..."),
                Arguments.of("1123232312"),
                Arguments.of("pl.gmail@reversed"),
                Arguments.of("name.surname@domain..com"),
                Arguments.of("comma,coma@gmail.com"),
                Arguments.of("space -space@gmail.com")
        );
    }

    private static Stream<Arguments> provideIncorrectPasswords() {
        return Stream.of(
                Arguments.of("1234567", "Password is too short"),
                Arguments.of("abcdef", "Password is too short"),
                Arguments.of("", "Password is empty"),
                // max password length is 50
                Arguments.of("123456789012345678901234567890123456789012345678901", "Password is too long"),
                Arguments.of("abcdefgh", "Password does not contain any digit"),
                Arguments.of("12345678", "Password does not contain any letter"),
                Arguments.of("1234567a", "Password does not contain any capital letter"),
                Arguments.of("1234567A", "Password does not contain any small letter"),
                Arguments.of("1234567Aa", "Password does not contain any special character"),
                Arguments.of("AbCdEfGh", "Password does not contain any digit"),
                Arguments.of("!#$%@#", "Password does not contain any letter")
        );
    }

    @AfterEach
    public void setup() {
        userRepository.deleteAll();
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("POST /api/v1/users - registration - Success")
    public void register_ShouldCreateNewUserInDBAndReturnCorrectUserDTOAndCorrectHeaders_GivenCorrectRegisterDTO() throws Exception {
        // prepare test data
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setName("test_name");
        registerDTO.setEmail("user@email.com");
        registerDTO.setPassword("test_password");

        log.info("Build registerDTO: {}", registerDTO);

        // perform the HTTP request and assert the response
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(registerDTO.getEmail()))
                .andExpect(jsonPath("$.name").value(registerDTO.getName()));

        // assert that user was saved in DB
        assertEquals(1, userRepository.count());

        Optional<User> DBUser = userRepository.findByEmail(registerDTO.getEmail());

        assertTrue(DBUser.isPresent(), "User was not found by email. Optional was empty.");

        User user = DBUser.get();

        log.info("User from DB: {}", user);

        assertEquals(registerDTO.getName(), user.getName());
        assertEquals(registerDTO.getEmail(), user.getEmail());
        // TODO: 2023-06-13 check if password is hashed
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectEmails")
    @NullAndEmptySource
    @DisplayName("POST /api/v1/users - registration - (400) BadRequest - Incorrect email")
    public void register_ShouldReturnBadRequestStatus_GivenIncorrectEmail(String email) throws Exception {
        // prepare test data
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setName("test_name");
        registerDTO.setPassword("test_password");
        // email is incorrect
        registerDTO.setEmail(email);

        log.info("Build registerDTO: {}", registerDTO);

        // perform the HTTP request and assert the response
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is incorrect"));

        // assert that user was not saved in DB
        assertEquals(0, userRepository.count(), "User was saved to DB!");
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectPasswords")
    @DisplayName("POST /api/v1/users - registration - (400) BadRequest - Incorrect password")
    public void register_ShouldReturnBadRequestStatus_GivenIncorrectPassword(String password, String details) throws Exception {
        // prepare test data
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setName("test_name");
        registerDTO.setEmail("user@email.com");
        // password is incorrect
        registerDTO.setPassword(password);

        log.info("Build registerDTO: {}", registerDTO);

        // perform the HTTP request and assert the response
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrect password"))
                .andExpect(jsonPath("$.details").value(details));

        // assert that user was not saved in DB
        assertEquals(0, userRepository.count(), "User was saved to DB!");
    }

    // TODO: 2023-06-14 replace with @ParameterizedTest
    // TODO: 2023-06-14 add test for empty name
    // TODO: 2023-06-14 add test for null name
    // TODO: 2023-06-14 add test for too long name
    @Test
    @DisplayName("POST /api/v1/users - registration - (400) BadRequest - Incorrect name")
    public void register_ShouldReturnBadRequestStatus_GivenIncorrectName() throws Exception {
        String details = "Name is empty";

        // prepare test data
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail("test_email");
        registerDTO.setPassword("test_password");
        // name is incorrect
        registerDTO.setName("");

        log.info("Build registerDTO: {}", registerDTO);

        // perform the HTTP request and assert the response
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrect name"))
                .andExpect(jsonPath("$.details").value(details));

        // assert that user was not saved in DB
        assertEquals(0, userRepository.count());
    }

    // TODO: 2023-06-14 replace with @ParameterizedTest
    @Test
    @DisplayName("POST /api/v1/users - registration - (400) BadRequest - Incorrect name, email and password")
    public void register_ShouldReturnBadRequestStatus_GivenIncorrectNameEmailAndPassword() throws Exception {
        String details = "Name is empty, email is incorrect, password is empty";

        // prepare test data
        RegisterDTO registerDTO = new RegisterDTO();
        // name is incorrect
        registerDTO.setName("");
        // email is incorrect
        registerDTO.setEmail("incorrect_email");
        // password is incorrect
        registerDTO.setPassword("");

        log.info("Build registerDTO: {}", registerDTO);

        // perform the HTTP request and assert the response
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reason").value("Incorrect name, email and password"))
                .andExpect(jsonPath("$.details").value(details));

        // assert that user was not saved in DB
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - Success")
    public void get_ShouldReturnCorrectUserDTO_GivenCorrectId() throws Exception {
        // prepare test data
        User user = new User();
        user.setName("test_name");
        user.setEmail("test_email");
        user.setPassword("test_password");

        log.info("Build user: {}", user);

        // save test data
        User savedUser = userRepository.save(user);

        log.info("Saved user: {}", savedUser);

        // assert that user was saved in DB
        assertEquals(1, userRepository.count());
        assertEquals(user.getName(), savedUser.getName());
        assertEquals(user.getEmail(), savedUser.getEmail());
        // TODO: 2023-06-13 check if password is hashed

        // perform the HTTP request and assert the response
        mockMvc.perform(get("/api/v1/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value(savedUser.getName()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - (404) NotFound - Incorrect id")
    public void get_ShouldReturnNotFoundStatus_GivenIncorrectId() throws Exception {
        // database is empty - it is cleared in @AfterEach method
        int id = 1;

        log.info("Incorrect id: {}", id);

        // perform the HTTP request and assert the response
        mockMvc.perform(get("/api/v1/users/{id}", id)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.reason").value("User not found with id: " + id));
    }
}