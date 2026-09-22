package vn.iotstar.service;

import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;

public interface AuthService {
    void register(RegisterDTO dto);
    boolean verifyAccount(String email, String otp);
    void resendOtp(String email);
    void forgotPassword(String email);
    boolean resetPassword(ResetPasswordDTO dto);
}
