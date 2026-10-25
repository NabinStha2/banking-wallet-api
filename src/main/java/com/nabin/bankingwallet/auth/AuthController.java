package com.nabin.bankingwallet.auth;


import com.nabin.bankingwallet.auth.dto.request.LoginRequest;
import com.nabin.bankingwallet.auth.dto.request.RegisterRequestDto;
import com.nabin.bankingwallet.auth.dto.request.ResendVerificationRequest;
import com.nabin.bankingwallet.auth.dto.request.VerifyEmailRequest;
import com.nabin.bankingwallet.auth.dto.response.LoginResponse;
import com.nabin.bankingwallet.auth.verification.EmailVerificationService;
import com.nabin.bankingwallet.user.UserService;
import com.nabin.bankingwallet.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final EmailVerificationService emailVerificationService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid
            @RequestBody
            RegisterRequestDto registerRequestDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(registerRequestDto));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
            @Valid
            @RequestBody
            VerifyEmailRequest request) {
        emailVerificationService.verify(request.token());
        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(
            @Valid
            @RequestBody
            ResendVerificationRequest request) {
        emailVerificationService.resendVerification(request.email());
        return ResponseEntity
                .accepted()
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid
            @RequestBody
            LoginRequest loginRequest) {
        return ResponseEntity
                .ok()
                .body(authService.login(loginRequest));
    }
}
