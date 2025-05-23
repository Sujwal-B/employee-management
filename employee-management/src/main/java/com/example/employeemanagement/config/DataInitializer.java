package com.example.employeemanagement.config;

import com.example.employeemanagement.model.security.User;
import com.example.employeemanagement.repository.security.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByUsername("adminuser").isEmpty()) {
            User admin = new User();
            admin.setUsername("adminuser");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setRoles("ROLE_ADMIN,ROLE_USER");
            userRepository.save(admin);
        }

        if (userRepository.findByUsername("regularuser").isEmpty()) {
            User user = new User();
            user.setUsername("regularuser");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRoles("ROLE_USER");
            userRepository.save(user);
        }
    }
}
