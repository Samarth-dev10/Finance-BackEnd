package com.finance.user.controller;

import com.finance.model.FinResponse;
import com.finance.model.PagedRs;
import com.finance.user.model.*;
import com.finance.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinResponse<UserRs>> createUser(
            @Valid @RequestBody UserCreateRq createRq) {

        FinResponse<UserRs> response = userService.createUser(createRq);
        HttpStatus status = response.isSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinResponse<PagedRs<UserSummaryRs>>> getAllUsers(
            @RequestParam(required = false)           String query,
            @RequestParam(defaultValue = "0")         int page,
            @RequestParam(defaultValue = "20")        int size) {

        return ResponseEntity.ok(userService.getAllUsers(query, page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    public ResponseEntity<FinResponse<UserRs>> getUserById(@PathVariable Long id) {
        FinResponse<UserRs> response = userService.getUserById(id);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinResponse<UserRs>> editUser(
            @PathVariable Long id,
            @Valid @RequestBody UserEditRq editRq) {

        FinResponse<UserRs> response = userService.editUser(id, editRq);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinResponse<UserRs>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRq statusRq) {

        FinResponse<UserRs> response = userService.updateUserStatus(id, statusRq);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FinResponse<Void>> deleteUser(@PathVariable Long id) {
        FinResponse<Void> response = userService.deleteUser(id);
        HttpStatus status = response.isSuccess() ? HttpStatus.NO_CONTENT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }
}