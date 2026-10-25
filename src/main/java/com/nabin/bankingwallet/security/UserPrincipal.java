package com.nabin.bankingwallet.security;

import com.nabin.bankingwallet.user.Role;
import com.nabin.bankingwallet.user.User;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * The authenticated user as Spring Security sees it. Controllers receive it via @AuthenticationPrincipal.
 */
@Getter
public class UserPrincipal implements UserDetails {
    private final String email;
    private final String passwordHash;
    private final boolean enabled;
    private final Role userRole;
    private final Long id;

    public UserPrincipal(User user) {
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.enabled = user.isEnabled() && user.isEmailVerified();
        this.userRole = user.getUserRole();
        this.id = user.getId();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
