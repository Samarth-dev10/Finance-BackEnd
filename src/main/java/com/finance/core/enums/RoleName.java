package com.finance.core.enums;

/**
 * RoleName — defines every access level the system supports.
 *
 * Data flow: RoleEntity.name (DB column) ↔ this enum.
 * Spring Security uses "ROLE_" prefix automatically when you call .roles(…).
 *
 *  VIEWER   → read-only; can see summary dashboard
 *  ANALYST  → read + analytics; cannot modify records or users
 *  ADMIN    → full control; create/edit/delete everything
 */
public enum RoleName {

    VIEWER,
    ANALYST,
    ADMIN
}
