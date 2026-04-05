package com.finance.user.model;

import com.finance.core.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserStatusRq {

    @NotNull(message = "Status is required")
    private UserStatus status;
}