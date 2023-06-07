package com.szymon_jasinski.eventplanner.controllers;

import com.szymon_jasinski.eventplanner.dtos.RegisterDTO;
import com.szymon_jasinski.eventplanner.dtos.UserDTO;
import com.szymon_jasinski.eventplanner.entities.User;
import com.szymon_jasinski.eventplanner.services.UserService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final ModelMapper modelMapper;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDTO> register(@RequestBody @Valid RegisterDTO registerDTO) {
        User user = userService.create(modelMapper.map(registerDTO, User.class));
        UserDTO createdUserDTO = modelMapper.map(user, UserDTO.class);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/api/v1/users/{id}")
                .buildAndExpand(createdUserDTO.getId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(createdUserDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> get(@PathVariable Long id) {
        UserDTO userDTO = modelMapper.map(userService.get(id), UserDTO.class);
        return ResponseEntity
                .ok()
                .body(userDTO);
    }
}
