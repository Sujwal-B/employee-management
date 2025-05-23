package de.zeroco.employeemanagement.config;

import de.zeroco.employeemanagement.security.JwtRequestFilter;
import de.zeroco.employeemanagement.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl; // Remains, Spring Security will use it

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // AuthenticationManagerBuilder is no longer used directly like this.
    // UserDetailsService and PasswordEncoder beans are picked up by Spring Security.

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/h2-console/**",
                    // Swagger UI / OpenAPI docs
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // Employees
                .requestMatchers(HttpMethod.GET, "/api/v1/employees/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/employees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/employees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/**").hasRole("ADMIN")
                // Departments
                .requestMatchers(HttpMethod.GET, "/api/v1/departments/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/departments/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/departments/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/departments/**").hasRole("ADMIN")
                // Projects
                .requestMatchers(HttpMethod.GET, "/api/v1/projects/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/projects/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/projects/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/projects/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        // For H2 console to work with Spring Security
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));
        return http.build();
    }
}
