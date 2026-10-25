package com.nabin.bankingwallet.config;


import com.nabin.bankingwallet.user.Role;
import com.nabin.bankingwallet.user.User;
import com.nabin.bankingwallet.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first ADMIN user from ADMIN_EMAIL / ADMIN_PASSWORD, if both are set and the user is missing.
 */
@Component
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder encoder,
                       @Value("${app.admin.email:}")
                       String email,
                       @Value("${app.admin.password:}")
                       String password) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || password.isBlank() ||
            userRepository.existsByEmail(email.toLowerCase())) {
            return;
        }
        User admin = new User();
        admin.setEmail(email.toLowerCase());
        admin.setFullName("Administrator");
        admin.setPasswordHash(encoder.encode(password));
        admin.setUserRole(Role.ADMIN);
        userRepository.save(admin);
    }
}