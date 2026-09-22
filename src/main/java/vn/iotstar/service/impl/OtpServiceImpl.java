package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.service.OtpService;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public String generateAndSaveOtp(String email, OtpType type) {
        // Sinh 6 chữ số ngẫu nhiên
        int number = 100000 + secureRandom.nextInt(900000);
        String otpCode = String.valueOf(number);

        OtpToken token = OtpToken.builder()
                .email(email.toLowerCase().trim())
                .otpCode(otpCode)
                .otpType(type)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        otpTokenRepository.save(token);
        return otpCode;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otpCode, OtpType type) {
        if (email == null || otpCode == null) {
            return false;
        }

        Optional<OtpToken> tokenOpt = otpTokenRepository.findByEmailAndOtpCodeAndOtpTypeAndUsedFalse(
                email.toLowerCase().trim(),
                otpCode.trim(),
                type
        );

        if (tokenOpt.isEmpty()) {
            return false;
        }

        OtpToken token = tokenOpt.get();
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            return false; // Hết hạn
        }

        token.setUsed(true);
        otpTokenRepository.save(token);
        return true;
    }
}
