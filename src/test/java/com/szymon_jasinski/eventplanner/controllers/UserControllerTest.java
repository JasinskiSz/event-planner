package com.szymon_jasinski.eventplanner.controllers;

import com.szymon_jasinski.eventplanner.dtos.RegisterDTO;
import com.szymon_jasinski.eventplanner.dtos.UserDTO;
import com.szymon_jasinski.eventplanner.entities.User;
import com.szymon_jasinski.eventplanner.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserControllerTest {
    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up mock Servlet request context
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(request));
    }

    @Test
    public void register_ShouldReturnCreatedStatusAndLocationAndBody() {
        // Prepare test data
        RegisterDTO registerDTO = new RegisterDTO();
        User user = new User();
        UserDTO userDTO = new UserDTO();
        userDTO.setId(1L);
        String basePath = "/api/v1/users";
        URI location = URI.create(basePath + "/" + userDTO.getId());

        // Mock the userService create method
        when(userService.create(any(User.class))).thenReturn(user);

        // Mock the modelMapper map method
        when(modelMapper.map(registerDTO, User.class)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        // Call the register method
        ResponseEntity<UserDTO> responseEntity = userController.register(registerDTO);

        // Verify the response
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(location.getPath(), responseEntity.getHeaders()
                .getLocation().getPath());
        assertEquals(userDTO, responseEntity.getBody());
    }

    @Test
    public void get_ShouldReturnOKStatusAndUserDTO() {
        // Prepare test data
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId);

        // Mock the userService get method
        when(userService.get(userId)).thenReturn(user);

        // Mock the modelMapper map method
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        // Call the get method
        ResponseEntity<UserDTO> responseEntity = userController.get(userId);

        // Verify the response
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userDTO, responseEntity.getBody());
    }
}