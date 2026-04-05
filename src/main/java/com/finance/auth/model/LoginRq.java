package com.finance.auth.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRq {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}