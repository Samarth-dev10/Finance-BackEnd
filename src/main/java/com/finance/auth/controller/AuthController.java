package com.finance.auth.controller;

import com.finance.auth.model.LoginRq;
import com.finance.auth.model.LoginRs;
import com.finance.auth.service.AuthService;
import com.finance.model.FinResponse;
import com.finance.user.model.UserRs;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<FinResponse<LoginRs>> login(
            @Valid @RequestBody LoginRq loginRq) {

        FinResponse<LoginRs> response = authService.login(loginRq);
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status).body(response);
    }


    @GetMapping("/me")
    public ResponseEntity<FinResponse<UserRs>> getCurrentUserProfile() {
        FinResponse<UserRs> response = authService.getCurrentUserProfile();
        HttpStatus status = response.isSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }
}