package com.szymon_jasinski.eventplanner.controllers;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
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