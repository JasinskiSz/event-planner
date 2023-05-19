package com.szymon_jasinski.eventplanner.dtos;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String password;
    private String name;
}
