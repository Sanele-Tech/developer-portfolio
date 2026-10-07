package com.sanele.portfolio.auth.service;

import com.sanele.portfolio.users.entity.Role;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.RoleRepository;
import com.sanele.portfolio.users.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminSeeder implements CommandLineRunner {
    private final Environment environment;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSeeder(Environment environment, UserRepository userRepository,
            RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.environment = environment;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String username = environment.getProperty("ADMIN_USERNAME");
        String password = environment.getProperty("ADMIN_PASSWORD");
        if (username == null || username.isBlank() || password == null || password.isBlank()) return;
        if (userRepository.existsByUsername(username)) return;

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new IllegalStateException("ADMIN role is not available"));
        String email = environment.getProperty("ADMIN_EMAIL", username + "@localhost");
        String displayName = environment.getProperty("ADMIN_DISPLAY_NAME", username);
        User admin = new User(username, email, passwordEncoder.encode(password), displayName);
        admin.addRole(adminRole);
        userRepository.save(admin);
    }
}