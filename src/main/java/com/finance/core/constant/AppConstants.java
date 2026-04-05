package com.finance.core.constant;

public final class AppConstants {

    private AppConstants() { /* utility class */ }

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final int MAX_PAGE_SIZE = 100;

    public static final String BEARER_PREFIX = "Bearer ";

    public static final String AUTH_HEADER = "Authorization";

    public static final String SYSTEM_USER = "system";

    //  Role name strings (mirrors RoleName enum, useful in @PreAuthorize)
    public static final String ROLE_ADMIN   = "ROLE_ADMIN";
    public static final String ROLE_ANALYST = "ROLE_ANALYST";
    public static final String ROLE_VIEWER  = "ROLE_VIEWER";
}