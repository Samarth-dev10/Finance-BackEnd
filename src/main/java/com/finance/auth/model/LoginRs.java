package com.finance.auth.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRs {

    private String token;
    private String tokenType;

    private Long   userId;
    private String username;
    private String fullName;
    private String role;
    private Long   expiresIn;
}