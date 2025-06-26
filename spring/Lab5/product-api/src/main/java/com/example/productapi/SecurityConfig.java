package com.example.productapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults; // For httpBasic()

@Configuration // Marks this class as a Spring configuration class
@EnableWebSecurity // Enables Spring Security's web security features
public class SecurityConfig {

    /**
     * Configures the security filter chain, defining authorization rules.
     * For Spring Boot 3+, this replaces WebSecurityConfigurerAdapter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Require authentication for all requests to /api/products and its sub-paths
                .requestMatchers("/api/products/**").authenticated()
                // Allow access to H2 Console without authentication (for development)
                .requestMatchers("/h2-console/**").permitAll()
                // Any other request requires authentication (can be changed to .permitAll() if desired)
                .anyRequest().authenticated()
            )
            // Use HTTP Basic authentication for secured endpoints
            .httpBasic(withDefaults())
            // Disable CSRF (Cross-Site Request Forgery) protection for simplicity in this API lab.
            // In a real-world web application (especially with browsers), CSRF protection is crucial.
            // For stateless REST APIs, it's often disabled if other security measures are in place.
            .csrf(csrf -> csrf.disable())
            // Configure header options, specifically to allow H2 Console in a frame
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin())); // Allow H2 Console to work in an iframe

        return http.build();
    }

    /**
     * Defines in-memory users for authentication.
     * In a real application, this would typically involve a database (e.g., using Spring Data JPA)
     * and a custom UserDetailsService to retrieve user details.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        // Define a 'user' with role "USER"
        UserDetails user = User.withUsername("user")
            .password(passwordEncoder.encode("password")) // Password encoded
            .roles("USER")
            .build();

        // Define an 'admin' with roles "ADMIN" and "USER"
        UserDetails admin = User.withUsername("admin")
            .password(passwordEncoder.encode("adminpass")) // Password encoded
            .roles("ADMIN", "USER")
            .build();

        // Store users in an in-memory manager
        return new InMemoryUserDetailsManager(user, admin);
    }

    /**
     * Defines the password encoder used to encode and verify passwords.
     * BCryptPasswordEncoder is a strong, recommended password hashing function.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
