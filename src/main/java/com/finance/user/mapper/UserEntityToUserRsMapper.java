package com.finance.user.mapper;

import com.finance.core.entity.UserEntity;
import com.finance.user.model.UserRs;

import java.util.function.Function;


public class UserEntityToUserRsMapper implements Function<UserEntity, UserRs> {

    public static final UserEntityToUserRsMapper INSTANCE = new UserEntityToUserRsMapper();

    private UserEntityToUserRsMapper() {}

    @Override
    public UserRs apply(UserEntity entity) {
        if (entity == null) return null;

        // Build the embedded RoleInfo object (null-safe)
        UserRs.RoleInfo roleInfo = null;
        if (entity.getRole() != null) {
            roleInfo = UserRs.RoleInfo.builder()
                    .id(entity.getRole().getId())
                    .name(entity.getRole().getName().name())   // enum → String
                    .build();
        }

        return UserRs.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                // password intentionally omitted — never expose it
                .fullName(entity.getFullName())
                .status(entity.getStatus().name())     // enum → String
                .role(roleInfo)
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}