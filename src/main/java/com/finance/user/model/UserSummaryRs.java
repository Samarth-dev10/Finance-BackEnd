package com.finance.user.model;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class UserSummaryRs {

    private Long id;
    private String username;
    private String fullName;
    private String email;

    private String status;

    private String roleName;
}