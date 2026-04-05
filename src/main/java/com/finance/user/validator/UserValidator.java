package com.finance.user.validator;

import com.finance.core.constant.ErrorCodes;
import com.finance.core.validator.BaseValidator;
import com.finance.user.model.UserCreateRq;
import com.finance.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator extends BaseValidator {

    private final UserRepository userRepository;


    @Override
    public void validate(Object request) {
        reset();   // clear any errors from a previous call

        if (!(request instanceof UserCreateRq rq)) {
            addError(ErrorCodes.VALIDATION_ERROR, null, "Invalid request type for UserValidator");
            return;
        }

        // Rule 1: username uniqueness
        if (rq.getUsername() != null
                && userRepository.existsByUsernameAndIsActiveTrue(rq.getUsername())) {
            addError(ErrorCodes.DUPLICATE_ENTRY, "username",
                    "Username '" + rq.getUsername() + "' is already taken");
        }

        // Rule 2: email uniqueness
        if (rq.getEmail() != null
                && userRepository.existsByEmailAndIsActiveTrue(rq.getEmail())) {
            addError(ErrorCodes.DUPLICATE_ENTRY, "email",
                    "Email '" + rq.getEmail() + "' is already registered");
        }
    }
}