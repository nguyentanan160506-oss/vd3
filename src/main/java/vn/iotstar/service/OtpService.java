package vn.iotstar.service;

import vn.iotstar.entity.OtpType;

public interface OtpService {
    String generateAndSaveOtp(String email, OtpType type);
    boolean verifyOtp(String email, String otpCode, OtpType type);
}
