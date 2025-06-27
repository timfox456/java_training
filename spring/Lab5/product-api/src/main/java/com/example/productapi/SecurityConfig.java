package com.example.productapi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // Added for HttpMethod.OPTIONS
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults; // For httpBasic() and cors()

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
            // 1. Enable CORS support within Spring Security. This tells Spring Security
            //    to look for a CorsConfigurationSource (provided by @CrossOrigin or a WebMvcConfigurer bean)
            //    and apply those rules. This must come before CSRF.
            .cors(withDefaults()) // <--- CRUCIAL FOR CORS INTEGRATION

            // 2. Disable CSRF (Cross-Site Request Forgery) protection for simplicity in this API lab.
            //    In a real-world web application (especially with browsers), CSRF protection is crucial.
            //    For stateless REST APIs, it's often disabled if other security measures are in place.
            .csrf(csrf -> csrf.disable())

            // 3. Define authorization rules for HTTP requests
            .authorizeHttpRequests(authorize -> authorize
                // 3a. IMPORTANT: Allow all OPTIONS requests (CORS preflight) without authentication.
                //    This rule MUST come BEFORE any more restrictive rules for the same path.
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // <--- CRUCIAL FOR PREFLIGHT

                // 3b. Allow access to H2 Console without authentication (for development)
                .requestMatchers("/h2-console/**").permitAll()

                // 3c. Require authentication for all requests to /api/products and its sub-paths
                //     If you want to test the product API without authentication temporarily,
                //     you can change this to .permitAll() and revert it later.
                .requestMatchers("/api/products/**").authenticated() // Or .hasRole("USER") etc.

                // 3d. Any other request also requires authentication by default
                .anyRequest().authenticated()
            )
            // 4. Use HTTP Basic authentication for secured endpoints
            .httpBasic(withDefaults())

            // 5. Configure header options, specifically to allow H2 Console in an iframe
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

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
