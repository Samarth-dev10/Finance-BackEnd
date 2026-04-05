package com.finance.user.serviceImpl;

import com.finance.core.constant.ErrorCodes;
import com.finance.core.entity.UserEntity;
import com.finance.core.enums.UserStatus;
import com.finance.core.utility.Utility;
import com.finance.model.FinResponse;
import com.finance.model.PagedRs;
import com.finance.role.repository.RoleRepository;
import com.finance.user.mapper.UserEntityToUserRsMapper;
import com.finance.user.mapper.UserEntityToUserSummaryRsMapper;
import com.finance.user.model.*;
import com.finance.user.repository.UserRepository;
import com.finance.user.service.UserService;
import com.finance.user.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository    userRepository;
    private final RoleRepository    roleRepository;
    private final UserValidator     userValidator;
    private final PasswordEncoder   passwordEncoder;


    @Override
    @Transactional          // if anything fails, the whole operation rolls back
    public FinResponse<UserRs> createUser(UserCreateRq createRq) {
        log.debug("Creating user with username={}", createRq.getUsername());

        // Step 1: Business-rule validation (uniqueness checks)
        userValidator.validate(createRq);
        if (userValidator.hasErrors()) {
            return FinResponse.failure("User creation failed", userValidator.getErrors());
        }

        // Step 2: Verify the requested role exists
        var roleOpt = roleRepository.findById(createRq.getRoleId());
        if (roleOpt.isEmpty()) {
            return FinResponse.failure("User creation failed",
                    Utility.createError(ErrorCodes.NOT_FOUND, "roleId",
                            "Role not found with id: " + createRq.getRoleId()));
        }

        try {
            // Step 3: Build the entity
            UserEntity user = new UserEntity();
            user.setUsername(createRq.getUsername());
            user.setEmail(createRq.getEmail());
            user.setPassword(passwordEncoder.encode(createRq.getPassword())); // HASH
            user.setFullName(createRq.getFullName());
            user.setRole(roleOpt.get());
            user.setStatus(UserStatus.ACTIVE);

            // Step 4: Stamp audit fields (createdBy, updatedBy)
            Utility.setAuditFields(user);

            // Step 5: Persist
            UserEntity saved = userRepository.save(user);
            log.info("User created: id={}, username={}", saved.getId(), saved.getUsername());

            return FinResponse.success("User created successfully",
                    UserEntityToUserRsMapper.INSTANCE.apply(saved));

        } catch (Exception ex) {
            log.error("Error creating user: {}", ex.getMessage(), ex);
            FinResponse<UserRs> response = new FinResponse<>();
            response.addError(Utility.internalError("Failed to create user"));
            return response;
        }
    }


    @Override
    public FinResponse<UserRs> getUserById(Long id) {
        log.debug("Fetching user id={}", id);

        return userRepository.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .map(u -> FinResponse.success("User fetched successfully",
                        UserEntityToUserRsMapper.INSTANCE.apply(u)))
                .orElseGet(() -> {
                    FinResponse<UserRs> r = new FinResponse<>();
                    r.addError(Utility.createError(ErrorCodes.NOT_FOUND,
                            "User not found with id: " + id));
                    return r;
                });
    }


    @Override
    public FinResponse<PagedRs<UserSummaryRs>> getAllUsers(String query, int page, int size) {
        log.debug("Fetching users page={} size={} query='{}'", page, size, query);

        try {
            // Clamp page size to prevent abuse (e.g., size=99999)
            int safeSize = Math.min(size, 100);
            Pageable pageable = PageRequest.of(page, safeSize);

            // Use search if a query is provided, otherwise fetch all
            Page<UserEntity> entityPage = (query != null && !query.isBlank())
                    ? userRepository.searchUsers(query.trim(), pageable)
                    : userRepository.findAllActiveUsers(pageable);

            // Map entity page → summary response page
            PagedRs<UserSummaryRs> pagedRs = PagedRs.<UserSummaryRs>builder()
                    .content(entityPage.getContent().stream()
                            .map(UserEntityToUserSummaryRsMapper.INSTANCE)
                            .toList())
                    .page(entityPage.getNumber())
                    .size(entityPage.getSize())
                    .totalElements(entityPage.getTotalElements())
                    .totalPages(entityPage.getTotalPages())
                    .last(entityPage.isLast())
                    .build();

            return FinResponse.success("Users fetched successfully", pagedRs);

        } catch (Exception ex) {
            log.error("Error fetching users: {}", ex.getMessage(), ex);
            FinResponse<PagedRs<UserSummaryRs>> r = new FinResponse<>();
            r.addError(Utility.internalError("Failed to fetch users"));
            return r;
        }
    }

    @Override
    @Transactional
    public FinResponse<UserRs> editUser(Long id, UserEditRq editRq) {
        log.debug("Editing user id={}", id);

        // Find the user to update
        var userOpt = userRepository.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()));

        if (userOpt.isEmpty()) {
            FinResponse<UserRs> r = new FinResponse<>();
            r.addError(Utility.createError(ErrorCodes.NOT_FOUND, "User not found with id: " + id));
            return r;
        }

        UserEntity user = userOpt.get();

        // Check email uniqueness if the email is being changed
        if (editRq.getEmail() != null
                && !editRq.getEmail().equals(user.getEmail())
                && userRepository.existsByEmailAndIsActiveTrue(editRq.getEmail())) {
            return FinResponse.failure("Edit failed",
                    Utility.createError(ErrorCodes.DUPLICATE_ENTRY, "email",
                            "Email '" + editRq.getEmail() + "' is already in use"));
        }

        try {
            // Apply only the non-null fields from the request
            Utility.applyIfNotNull(editRq.getEmail(),    user::setEmail);
            Utility.applyIfNotNull(editRq.getFullName(), user::setFullName);

            // Role change — look up the new role if provided
            if (editRq.getRoleId() != null) {
                var roleOpt = roleRepository.findById(editRq.getRoleId());
                if (roleOpt.isEmpty()) {
                    return FinResponse.failure("Edit failed",
                            Utility.createError(ErrorCodes.NOT_FOUND, "roleId",
                                    "Role not found with id: " + editRq.getRoleId()));
                }
                user.setRole(roleOpt.get());
            }

            Utility.setAuditFields(user);
            UserEntity saved = userRepository.save(user);

            return FinResponse.success("User updated successfully",
                    UserEntityToUserRsMapper.INSTANCE.apply(saved));

        } catch (Exception ex) {
            log.error("Error editing user id={}: {}", id, ex.getMessage(), ex);
            FinResponse<UserRs> r = new FinResponse<>();
            r.addError(Utility.internalError("Failed to update user"));
            return r;
        }
    }


    @Override
    @Transactional
    public FinResponse<UserRs> updateUserStatus(Long id, UserStatusRq statusRq) {
        log.debug("Updating status for user id={} to {}", id, statusRq.getStatus());

        // Guard: prevent self-deactivation of the system admin (id=1)
        String currentUser = Utility.getCurrentUsername();
        var userOpt = userRepository.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()));

        if (userOpt.isEmpty()) {
            FinResponse<UserRs> r = new FinResponse<>();
            r.addError(Utility.createError(ErrorCodes.NOT_FOUND, "User not found with id: " + id));
            return r;
        }

        UserEntity user = userOpt.get();

        // Guard: admin cannot deactivate their own account
        if (user.getUsername().equals(currentUser)
                && statusRq.getStatus() == UserStatus.INACTIVE) {
            return FinResponse.failure("Status update failed",
                    Utility.createError(ErrorCodes.OPERATION_NOT_ALLOWED,
                            "You cannot deactivate your own account"));
        }

        try {
            user.setStatus(statusRq.getStatus());
            Utility.setAuditFields(user);
            UserEntity saved = userRepository.save(user);

            return FinResponse.success("User status updated successfully",
                    UserEntityToUserRsMapper.INSTANCE.apply(saved));

        } catch (Exception ex) {
            log.error("Error updating user status id={}: {}", id, ex.getMessage(), ex);
            FinResponse<UserRs> r = new FinResponse<>();
            r.addError(Utility.internalError("Failed to update user status"));
            return r;
        }
    }


    @Override
    @Transactional
    public FinResponse<Void> deleteUser(Long id) {
        log.debug("Soft-deleting user id={}", id);

        // Guard: system admin (id=1) must never be deleted
        if (id == 1L) {
            return FinResponse.failure("Delete failed",
                    Utility.createError(ErrorCodes.OPERATION_NOT_ALLOWED,
                            "The system administrator account cannot be deleted"));
        }

        var userOpt = userRepository.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()));

        if (userOpt.isEmpty()) {
            FinResponse<Void> r = new FinResponse<>();
            r.addError(Utility.createError(ErrorCodes.NOT_FOUND, "User not found with id: " + id));
            return r;
        }

        try {
            UserEntity user = userOpt.get();
            user.setIsActive(false);    // soft delete — row stays in DB
            user.setStatus(UserStatus.INACTIVE);
            Utility.setAuditFields(user);
            userRepository.save(user);

            log.info("User soft-deleted: id={}", id);
            return FinResponse.success("User deleted successfully");

        } catch (Exception ex) {
            log.error("Error deleting user id={}: {}", id, ex.getMessage(), ex);
            FinResponse<Void> r = new FinResponse<>();
            r.addError(Utility.internalError("Failed to delete user"));
            return r;
        }
    }
}