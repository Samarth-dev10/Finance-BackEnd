package com.finance.role.mapper;

import com.finance.core.entity.RoleEntity;
import com.finance.role.model.RoleRs;

import java.util.function.Function;


public class RoleEntityToRoleRsMapper implements Function<RoleEntity, RoleRs> {

    public static final RoleEntityToRoleRsMapper INSTANCE = new RoleEntityToRoleRsMapper();

    private RoleEntityToRoleRsMapper() {}

    @Override
    public RoleRs apply(RoleEntity entity) {
        if (entity == null) return null;

        return RoleRs.builder()
                .id(entity.getId())
                .name(entity.getName().name())   // enum → String: ADMIN → "ADMIN"
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .build();
    }
}