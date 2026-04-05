package com.finance;

import com.finance.auth.model.LoginRq;
import com.finance.auth.model.LoginRs;
import com.finance.auth.serviceImpl.AuthServiceImpl;
import com.finance.config.JwtUtil;
import com.finance.core.constant.ErrorCodes;
import com.finance.core.entity.RoleEntity;
import com.finance.core.entity.UserEntity;
import com.finance.core.enums.RoleName;
import com.finance.core.enums.UserStatus;
import com.finance.model.FinResponse;
import com.finance.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * AuthServiceImplTest — unit tests for login logic.
 * WHY unit tests (not integration)?
 *   → Fast: no Spring context startup, no real DB, runs in milliseconds
 *   → Focused: test ONLY AuthServiceImpl logic, mock everything else
 *   → Repeatable: no external dependencies, same result every time
 * Testing strategy:
 *   - Happy path (successful login)
 *   - Bad credentials
 *   - Inactive account
 * ExtendWith(MockitoExtension.class) activates Mockito annotations (@Mock, @InjectMocks)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Tests")
class AuthServiceImplTest {

    // ── Mocks: fake implementations we control ────────────────────────────────
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil               jwtUtil;
    @Mock private UserRepository        userRepository;
    @Mock private Authentication        authentication;

    // ── System Under Test: the real implementation being tested ───────────────
    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity testUser;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        // Inject the @Value field manually (no Spring context in unit tests)
        ReflectionTestUtils.setField(authService, "expirationMs", 86400000L);

        // Build a reusable test user entity
        RoleEntity adminRole = new RoleEntity();
        adminRole.setId(3L);
        adminRole.setName(RoleName.ADMIN);

        testUser = new UserEntity();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setEmail("admin@finance.com");
        testUser.setFullName("System Administrator");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setRole(adminRole);
        testUser.setIsActive(true);

        testUserDetails = new User("admin", "$2a$10$hashedpass",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    // ── Test: Successful Login ─────────────────────────────────────────────────

    @Test
    @DisplayName("login() — happy path: valid credentials return JWT token")
    void login_validCredentials_returnsToken() {
        // ARRANGE: set up what each mock should return
        LoginRq loginRq = new LoginRq();
        loginRq.setUsername("admin");
        loginRq.setPassword("admin123");

        // authenticationManager.authenticate() succeeds and returns an authentication object
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        // The authentication principal is our UserDetails
        when(authentication.getPrincipal()).thenReturn(testUserDetails);

        // The user is found in the database
        when(userRepository.findByUsernameAndIsActiveTrue("admin"))
                .thenReturn(Optional.of(testUser));

        // JWT generation returns a mock token string
        when(jwtUtil.generateToken(testUserDetails, "ADMIN"))
                .thenReturn("mock.jwt.token");

        // ACT: call the method under test
        FinResponse<LoginRs> response = authService.login(loginRq);

        // ASSERT: verify the response
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getData().getRole()).isEqualTo("ADMIN");
        assertThat(response.getData().getUsername()).isEqualTo("admin");
        assertThat(response.getData().getTokenType()).isEqualTo("Bearer");

        // VERIFY: the right methods were called
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, times(1)).generateToken(testUserDetails, "ADMIN");
    }

    // ── Test: Bad Credentials ─────────────────────────────────────────────────

    @Test
    @DisplayName("login() — bad credentials: returns INVALID_CREDENTIALS error")
    void login_badCredentials_returnsError() {
        // ARRANGE
        LoginRq loginRq = new LoginRq();
        loginRq.setUsername("admin");
        loginRq.setPassword("wrongpassword");

        // authenticationManager throws BadCredentialsException (Spring Security behaviour)
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // ACT
        FinResponse<LoginRs> response = authService.login(loginRq);

        // ASSERT
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors().get(0).getCode())
                .isEqualTo(ErrorCodes.INVALID_CREDENTIALS);

        // The JWT should never be generated for a failed login
        verify(jwtUtil, never()).generateToken(any(), any());
    }

    // ── Test: Inactive Account ────────────────────────────────────────────────

    @Test
    @DisplayName("login() — inactive account: returns ACCOUNT_INACTIVE error")
    void login_inactiveAccount_returnsError() {
        // ARRANGE
        LoginRq loginRq = new LoginRq();
        loginRq.setUsername("admin");
        loginRq.setPassword("admin123");

        // Simulate our UserDetailsService throwing for an inactive user
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Account is inactive: admin"));

        // ACT
        FinResponse<LoginRs> response = authService.login(loginRq);

        // ASSERT
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors().get(0).getCode())
                .isEqualTo(ErrorCodes.ACCOUNT_INACTIVE);
    }
}
