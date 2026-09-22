package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, String subject) {
        log.info("==================================================");
        log.info("GỬI MÃ OTP ĐẾN EMAIL: {}", toEmail);
        log.info("TIÊU ĐỀ: {}", subject);
        log.info("MÃ OTP: {}", otpCode);
        log.info("==================================================");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText("Xin chào,\n\nMã OTP xác thực của bạn là: " + otpCode
                    + "\n\nMã này có hiệu lực trong vòng 5 phút. Vui lòng không chia sẻ mã này cho người khác.\n\nTrân trọng,\nIOTSTAR Shop.");
            mailSender.send(message);
            log.info("Email OTP đã gửi thành công tới: {}", toEmail);
        } catch (Exception e) {
            log.warn("Không thể gửi email qua SMTP (có thể do chưa cấu hình app password): {}. OTP hiển thị ở log: {}",
                    e.getMessage(), otpCode);
        }
    }
}
