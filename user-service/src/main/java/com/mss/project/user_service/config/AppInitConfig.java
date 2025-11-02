package com.mss.project.user_service.config;

import com.mss.project.user_service.entity.User;
import com.mss.project.user_service.enums.Role;
import com.mss.project.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppInitConfig {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Bean
    ApplicationRunner init() {
        return args -> {
            if(userRepository.findByUsername("admin").isEmpty()){
                User user = new User();
                user.setUsername("admin");
                user.setEmail("admin@gmail.com");
                user.setPassword(passwordEncoder.encode("admin123"));
                user.setFirstName("Admin");
                user.setLastName("4Bus");
                user.setPhoneNumber("0123456789");
                user.setAddress("Admin House");
                user.setRole(Role.ADMIN);
                userRepository.save(user);
                log.info("Admin initialized with username: admin, password: admin123");
            }
        };
    }
}
