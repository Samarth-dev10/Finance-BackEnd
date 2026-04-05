package com.finance.user.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class UserRs {

    private Long id;
    private String username;
    private String email;
    private String fullName;

    private String status;

    private RoleInfo role;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    //Avoids creating a top-level class just for 2 fields.
    @Data
    @Builder
    public static class RoleInfo {
        private Long id;
        private String name;
    }
}