package com.szymon_jasinski.eventplanner.dtos;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@Data
public class RegisterDTO {
    @NotBlank
    private String name;
    @Email
    private String email;
    @NotEmpty
    private String password;
}
