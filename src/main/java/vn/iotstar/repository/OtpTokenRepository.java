package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndOtpTypeAndUsedFalseOrderByCreatedAtDesc(String email, OtpType otpType);

    Optional<OtpToken> findByEmailAndOtpCodeAndOtpTypeAndUsedFalse(String email, String otpCode, OtpType otpType);

    void deleteByExpiryTimeBefore(LocalDateTime time);
}
