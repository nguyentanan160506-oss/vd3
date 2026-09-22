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
            @Value("${ADMIN_EMAIL:trungnh@hcmute.edu.vn}") String adminEmail,
            @Value("${ADMIN_PASSWORD:123456}") String adminPassword,
            @Value("${ADMIN_USERNAME:admin}") String adminUsername
    ) {
        return args -> {
            Role userRole = roleRepository.findByNameIgnoreCase("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

            Role adminRole = roleRepository.findByNameIgnoreCase("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

            // Tạo Admin nếu chưa có
            if (!userRepository.existsByEmailIgnoreCase(adminEmail) && !userRepository.existsByUsernameIgnoreCase(adminUsername)) {
                User admin = User.builder()
                        .username(adminUsername)
                        .email(adminEmail.toLowerCase())
                        .fullName("System Administrator")
                        .password(passwordEncoder.encode(adminPassword))
                        .role(adminRole)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(admin);
                log.info(">>> Đã khởi tạo tài khoản ADMIN mặc định: {} / {}", adminEmail, adminPassword);
            }

            // Tạo User mẫu nếu chưa có
            if (!userRepository.existsByUsernameIgnoreCase("user01")) {
                User sampleUser = User.builder()
                        .username("user01")
                        .email("user01@gmail.com")
                        .fullName("Nguyễn Hữu Trung")
                        .password(passwordEncoder.encode("123456"))
                        .role(userRole)
                        .enabled(true)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(sampleUser);
                log.info(">>> Đã khởi tạo tài khoản USER mẫu: user01 / 123456");
            }
        };
    }
}
