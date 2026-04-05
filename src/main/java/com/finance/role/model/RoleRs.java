package com.finance.role.model;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class RoleRs {

    private Long id;

    private String name;

    private String description;

    private Boolean isActive;
}