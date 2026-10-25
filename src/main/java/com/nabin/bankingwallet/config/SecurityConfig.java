package com.nabin.bankingwallet.config;

import com.nabin.bankingwallet.user.UserRepository;
import com.nabin.bankingwallet.user.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/ping",
                                "/actuator/health/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/verify-email.html"
                        ).permitAll()
                        .anyRequest().authenticated())
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> {
            User user = userRepository.findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
            return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                    .password(user.getPasswordHash())
                    .authorities("ROLE_" + user.getUserRole().name())
                    .disabled(!user.isEnabled() || !user.isEmailVerified())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with strength 12 - each increment roughly doubles hashing
        // time, which is the point (slows down brute-force attempts).
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Only these origins may call the API from a browser.
        // Update this list as your frontend domains change.
        configuration.setAllowedOrigins(
                Arrays.asList("http://localhost:3000", "http://localhost:8080",
                              "https://yourdomain.com"));

        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
