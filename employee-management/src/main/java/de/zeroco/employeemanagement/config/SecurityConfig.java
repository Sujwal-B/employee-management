package de.zeroco.employeemanagement.config;

import de.zeroco.employeemanagement.security.JwtRequestFilter;
import de.zeroco.employeemanagement.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsServiceImpl).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers(
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
            .antMatchers(HttpMethod.GET, "/api/v1/employees/**").hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.POST, "/api/v1/employees/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.PUT, "/api/v1/employees/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/api/v1/employees/**").hasRole("ADMIN")
            // Departments
            .antMatchers(HttpMethod.GET, "/api/v1/departments/**").hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.POST, "/api/v1/departments/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.PUT, "/api/v1/departments/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/api/v1/departments/**").hasRole("ADMIN")
            // Projects
            .antMatchers(HttpMethod.GET, "/api/v1/projects/**").hasAnyRole("USER", "ADMIN")
            .antMatchers(HttpMethod.POST, "/api/v1/projects/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.PUT, "/api/v1/projects/**").hasRole("ADMIN")
            .antMatchers(HttpMethod.DELETE, "/api/v1/projects/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        // For H2 console to work with Spring Security
        http.headers().frameOptions().sameOrigin();
    }
}
