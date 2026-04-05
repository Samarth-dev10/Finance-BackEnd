package com.finance.user.mapper;

import com.finance.core.entity.UserEntity;
import com.finance.user.model.UserSummaryRs;

import java.util.function.Function;

public class UserEntityToUserSummaryRsMapper implements Function<UserEntity, UserSummaryRs> {

    public static final UserEntityToUserSummaryRsMapper INSTANCE =
            new UserEntityToUserSummaryRsMapper();

    private UserEntityToUserSummaryRsMapper() {}

    @Override
    public UserSummaryRs apply(UserEntity entity) {
        if (entity == null) return null;

        String roleName = (entity.getRole() != null)
                ? entity.getRole().getName().name()
                : "N/A";

        return UserSummaryRs.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .status(entity.getStatus().name())
                .roleName(roleName)
                .build();
    }
}