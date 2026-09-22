package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.entity.OtpType;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.EmailService;
import vn.iotstar.service.OtpService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final EmailService emailService;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }

        if (userRepository.existsByUsernameIgnoreCase(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Username đã được sử dụng.");
        }

        if (userRepository.existsByEmailIgnoreCase(dto.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã được đăng ký.");
        }

        Role userRole = roleRepository.findByNameIgnoreCase("ROLE_USER")
                .or(() -> roleRepository.findByNameIgnoreCase("USER"))
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        String avatarUrl = null;
        if (dto.getAvatarFile() != null && !dto.getAvatarFile().isEmpty()) {
            avatarUrl = cloudinaryService.uploadImage(dto.getAvatarFile());
        }

        User user = User.builder()
                .username(dto.getUsername().trim())
                .email(dto.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName().trim())
                .images(avatarUrl)
                .role(userRole)
                .enabled(false) // Cần xác nhận OTP để kích hoạt
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // Sinh OTP và gửi mail
        String otp = otpService.generateAndSaveOtp(user.getEmail(), OtpType.REGISTER);
        emailService.sendOtpEmail(user.getEmail(), otp, "Xác thực tài khoản IOTSTAR SHOP");
    }

    @Override
    @Transactional
    public boolean verifyAccount(String email, String otp) {
        boolean valid = otpService.verifyOtp(email, otp, OtpType.REGISTER);
        if (!valid) {
            return false;
        }

        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElse(null);
        if (user == null) {
            return false;
        }

        user.setEnabled(true);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Email chưa được đăng ký trong hệ thống."));

        String otp = otpService.generateAndSaveOtp(user.getEmail(), OtpType.REGISTER);
        emailService.sendOtpEmail(user.getEmail(), otp, "Gửi lại mã OTP xác thực - IOTSTAR SHOP");
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống."));

        String otp = otpService.generateAndSaveOtp(user.getEmail(), OtpType.FORGOT_PASSWORD);
        emailService.sendOtpEmail(user.getEmail(), otp, "Mã OTP đặt lại mật khẩu - IOTSTAR SHOP");
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }

        boolean valid = otpService.verifyOtp(dto.getEmail(), dto.getOtp(), OtpType.FORGOT_PASSWORD);
        if (!valid) {
            return false;
        }

        User user = userRepository.findByEmailIgnoreCase(dto.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
        return true;
    }
}
