package com.szymon_jasinski.eventplanner.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;


@Data
public class RegisterDTO {
    @NotBlank
    private String name;
    @Email(message = "Email is not valid")
    private String email;
    @NotEmpty
    private String password;
}
