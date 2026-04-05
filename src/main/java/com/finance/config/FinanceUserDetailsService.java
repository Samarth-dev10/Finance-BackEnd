package com.finance.config;

import com.finance.core.entity.UserEntity;
import com.finance.core.enums.UserStatus;
import com.finance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository
                .findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username));

        // Reject INACTIVE users at the security layer
        if (userEntity.getStatus() == UserStatus.INACTIVE) {
            throw new UsernameNotFoundException("Account is inactive: " + username);
        }

        // Build the Spring Security authority string: "ROLE_ADMIN", "ROLE_VIEWER" etc.
        // Spring's hasRole('ADMIN') automatically prepends "ROLE_" when checking.
        String authorityString = "ROLE_" + userEntity.getRole().getName().name();

        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())     // the BCrypt hash stored in DB
                .authorities(new SimpleGrantedAuthority(authorityString))
                .build();
    }
}