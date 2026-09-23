package vn.iotstar.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

import java.time.LocalDateTime;

@Configuration
@Slf4j
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_EMAIL:admin@gmail.com}") String adminEmail,
            @Value("${ADMIN_PASSWORD:123456}") String adminPassword,
            @Value("${ADMIN_USERNAME:admin}") String adminUsername
    ) {
        return args -> {
            Role userRole = roleRepository.findByNameIgnoreCase("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

            Role adminRole = roleRepository.findByNameIgnoreCase("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

            // Tạo Admin nếu chưa có
            if (!userRepository.existsByUsernameIgnoreCase(adminUsername) && !userRepository.existsByEmailIgnoreCase(adminEmail)) {
                User admin = User.builder()
                        .username(adminUsername)
                        .email(adminEmail.toLowerCase())
                        .fullName("Administrator")
                        .images("/images/avatar-default.png")
                        .password(passwordEncoder.encode(adminPassword))
                        .role(adminRole)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(admin);
                log.info(">>> Đã khởi tạo tài khoản ADMIN mặc định: {} / {}", adminUsername, adminPassword);
            } else {
                userRepository.findByUsernameIgnoreCase(adminUsername).ifPresent(admin -> {
                    boolean updated = false;
                    if (!admin.getEmail().equalsIgnoreCase(adminEmail) && !userRepository.existsByEmailIgnoreCase(adminEmail)) {
                        admin.setEmail(adminEmail.toLowerCase());
                        updated = true;
                    }
                    if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
                        admin.setPassword(passwordEncoder.encode(adminPassword));
                        updated = true;
                    }
                    if (admin.getImages() == null || admin.getImages().isBlank()) {
                        admin.setImages("/images/avatar-default.png");
                        updated = true;
                    }
                    if (!admin.isEnabled()) {
                        admin.setEnabled(true);
                        updated = true;
                    }
                    if (updated) {
                        userRepository.save(admin);
                        log.info(">>> Đã đồng bộ tài khoản ADMIN: email={}, username={}", admin.getEmail(), admin.getUsername());
                    }
                });
            }

            // Tạo User mẫu nếu chưa có
            if (!userRepository.existsByUsernameIgnoreCase("user01") && !userRepository.existsByEmailIgnoreCase("user01@gmail.com")) {
                User sampleUser = User.builder()
                        .username("user01")
                        .email("user01@gmail.com")
                        .fullName("User Test")
                        .images("/images/avatar-default.png")
                        .password(passwordEncoder.encode("123456"))
                        .role(userRole)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(sampleUser);
                log.info(">>> Đã khởi tạo tài khoản USER mẫu: user01 / 123456");
            } else {
                userRepository.findByUsernameIgnoreCase("user01").ifPresent(user -> {
                    boolean updated = false;
                    if (!user.getEmail().equalsIgnoreCase("user01@gmail.com") && !userRepository.existsByEmailIgnoreCase("user01@gmail.com")) {
                        user.setEmail("user01@gmail.com");
                        updated = true;
                    }
                    if (!passwordEncoder.matches("123456", user.getPassword())) {
                        user.setPassword(passwordEncoder.encode("123456"));
                        updated = true;
                    }
                    if (user.getImages() == null || user.getImages().isBlank()) {
                        user.setImages("/images/avatar-default.png");
                        updated = true;
                    }
                    if (!user.isEnabled()) {
                        user.setEnabled(true);
                        updated = true;
                    }
                    if (updated) {
                        userRepository.save(user);
                        log.info(">>> Đã đồng bộ tài khoản USER mẫu: email={}, username={}", user.getEmail(), user.getUsername());
                    }
                });
            }
        };
    }
}
