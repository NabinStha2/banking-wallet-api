package com.nabin.bankingwallet.user;

import com.nabin.bankingwallet.auth.dto.request.RegisterRequestDto;
import com.nabin.bankingwallet.auth.verification.EmailVerificationService;
import com.nabin.bankingwallet.common.exception.DuplicateResourceException;
import com.nabin.bankingwallet.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String EMAIL_UNIQUE_CONSTRAINT = "uk_users_email";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public UserResponse register(RegisterRequestDto registerRequestDto) {
        String email = registerRequestDto
                .email()
                .trim()
                .toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFullName(registerRequestDto
                                    .fullName()
                                    .trim());
        newUser.setPasswordHash(passwordEncoder.encode(registerRequestDto.password()));
        newUser.setUserRole(Role.USER);
        newUser.setEmailVerified(false);

        try {
            newUser = userRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException exception) {
            if (violatesEmailUniqueConstraint(exception)) {
                throw duplicateEmail();
            }
            throw exception;
        }

        emailVerificationService.sendInitialVerification(newUser);

        return UserResponse.fromUser(newUser);
    }

    private boolean violatesEmailUniqueConstraint(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                return EMAIL_UNIQUE_CONSTRAINT.equalsIgnoreCase(violation.getConstraintName());
            }
        }
        return false;
    }

    private DuplicateResourceException duplicateEmail() {
        return new DuplicateResourceException("An account with this email already exists");
    }
}
