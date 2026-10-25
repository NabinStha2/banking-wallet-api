package com.nabin.bankingwallet.auth;

import com.nabin.bankingwallet.auth.dto.request.LoginRequest;
import com.nabin.bankingwallet.auth.dto.response.LoginResponse;
import com.nabin.bankingwallet.security.JwtService;
import com.nabin.bankingwallet.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest
                                                                .email()
                                                                .trim()
                                                                .toLowerCase(
                                                                        Locale.ROOT),
                                                        loginRequest.password()));
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = jwtService.generateToken(userPrincipal);
        return new LoginResponse(accessToken, "Bearer", jwtService.getExpirationSeconds());

    }
}
