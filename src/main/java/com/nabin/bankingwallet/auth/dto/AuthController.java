package com.nabin.bankingwallet.auth.dto;


import com.nabin.bankingwallet.auth.dto.request.RegisterRequestDto;
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

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid
            @RequestBody
            RegisterRequestDto registerRequestDto
                                                ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.register(registerRequestDto));
    }
}
