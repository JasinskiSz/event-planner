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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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