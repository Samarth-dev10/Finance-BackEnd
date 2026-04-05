package com.finance.auth.serviceImpl;

import com.finance.auth.model.LoginRq;
import com.finance.auth.model.LoginRs;
import com.finance.auth.service.AuthService;
import com.finance.config.JwtUtil;
import com.finance.core.constant.ErrorCodes;
import com.finance.core.utility.Utility;
import com.finance.model.FinResponse;
import com.finance.user.mapper.UserEntityToUserRsMapper;
import com.finance.user.model.UserRs;
import com.finance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil               jwtUtil;
    private final UserRepository        userRepository;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Override
    public FinResponse<LoginRs> login(LoginRq loginRq) {
        log.debug("Login attempt for username={}", loginRq.getUsername());

        try {
            // Step 1: Delegate credential verification to Spring Security.
            // This triggers FinanceUserDetailsService + BCrypt comparison internally.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRq.getUsername(),
                            loginRq.getPassword()
                    )
            );

            // Step 2: Extract the authenticated principal (UserDetails)
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Step 3: Load the full UserEntity for response enrichment (fullName, id, role)
            var userEntity = userRepository
                    .findByUsernameAndIsActiveTrue(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));

            // Step 4: Determine the role name to embed in the JWT
            String roleName = userEntity.getRole().getName().name();   // e.g., "ADMIN"

            // Step 5: Generate the signed JWT
            String token = jwtUtil.generateToken(userDetails, roleName);

            // Step 6: Build the response payload
            LoginRs loginRs = LoginRs.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(userEntity.getId())
                    .username(userEntity.getUsername())
                    .fullName(userEntity.getFullName())
                    .role(roleName)
                    .expiresIn(expirationMs)
                    .build();

            log.info("Login successful for username={}", loginRq.getUsername());
            return FinResponse.success("Login successful", loginRs);

        } catch (BadCredentialsException ex) {
            log.warn("Login failed for username={}: bad credentials", loginRq.getUsername());
            return FinResponse.failure("Authentication failed",
                    Utility.createError(ErrorCodes.INVALID_CREDENTIALS,
                            "Invalid username or password"));

        } catch (Exception ex) {
            log.error("Login error for username={}: {}", loginRq.getUsername(), ex.getMessage(), ex);

            // Check if it's an inactive account error from our UserDetailsService
            if (ex.getMessage() != null && ex.getMessage().contains("inactive")) {
                return FinResponse.failure("Authentication failed",
                        Utility.createError(ErrorCodes.ACCOUNT_INACTIVE,
                                "Your account is inactive. Please contact an administrator."));
            }

            return FinResponse.failure("Authentication failed",
                    Utility.internalError("An unexpected error occurred during login"));
        }
    }

    @Override
    public FinResponse<UserRs> getCurrentUserProfile() {
        String username = Utility.getCurrentUsername();
        log.debug("Fetching profile for current user={}", username);

        return userRepository.findByUsernameAndIsActiveTrue(username)
                .map(entity -> FinResponse.success(
                        "Profile fetched successfully",
                        UserEntityToUserRsMapper.INSTANCE.apply(entity)))
                .orElseGet(() -> {
                    FinResponse<UserRs> r = new FinResponse<>();
                    r.addError(Utility.createError(ErrorCodes.NOT_FOUND,
                            "Current user profile not found"));
                    return r;
                });
    }
}