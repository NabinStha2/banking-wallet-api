package com.nabin.bankingwallet.user.dto;

import com.nabin.bankingwallet.user.User;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String userRole,
        boolean emailVerified
) {
    public static UserResponse fromUser(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user
                        .getUserRole()
                        .name(),
                user.isEmailVerified()
        );
    }
}
