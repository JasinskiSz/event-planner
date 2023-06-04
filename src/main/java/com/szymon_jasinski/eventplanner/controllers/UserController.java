package com.szymon_jasinski.eventplanner.controllers;

import com.szymon_jasinski.eventplanner.dtos.UserDTO;
import com.szymon_jasinski.eventplanner.services.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final ModelMapper modelMapper;
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> get(@PathVariable Long id) {
        UserDTO userDTO = modelMapper.map(userService.get(id), UserDTO.class);
        return ResponseEntity
                .ok()
                .body(userDTO);
    }
}
