package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.*;
import vn.iotstar.service.AuthService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginDTO")) {
            model.addAttribute("loginDTO", new LoginDTO());
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerDTO") RegisterDTO registerDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(registerDTO);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Vui lòng nhập mã OTP đã gửi đến email " + registerDTO.getEmail() + " để kích hoạt tài khoản.");
            return "redirect:/verify-otp?email=" + registerDTO.getEmail();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/register";
        } catch (Exception ex) {
            log.error("Lỗi đăng ký: ", ex);
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại!");
            return "auth/register";
        }
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(value = "email", required = false) String email, Model model) {
        VerifyOtpDTO dto = VerifyOtpDTO.builder().email(email).build();
        model.addAttribute("verifyOtpDTO", dto);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(
            @Valid @ModelAttribute("verifyOtpDTO") VerifyOtpDTO verifyOtpDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/verify-otp";
        }

        boolean success = authService.verifyAccount(verifyOtpDTO.getEmail(), verifyOtpDTO.getOtp());
        if (success) {
            redirectAttributes.addFlashAttribute("verified", true);
            return "redirect:/login?verified=true";
        } else {
            model.addAttribute("errorMessage", "Mã OTP không đúng hoặc đã hết hạn (hiệu lực 5 phút).");
            return "auth/verify-otp";
        }
    }

    @PostMapping("/register/resend-otp")
    public String resendOtp(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            authService.resendOtp(email);
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP mới đã được gửi đến email của bạn.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/verify-otp?email=" + email;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        if (!model.containsAttribute("forgotPasswordDTO")) {
            model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO forgotPasswordDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            authService.forgotPassword(forgotPasswordDTO.getEmail());
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP đặt lại mật khẩu đã được gửi đến email của bạn.");
            return "redirect:/reset-password?email=" + forgotPasswordDTO.getEmail();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(value = "email", required = false) String email, Model model) {
        ResetPasswordDTO dto = ResetPasswordDTO.builder().email(email).build();
        model.addAttribute("resetPasswordDTO", dto);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO resetPasswordDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        try {
            boolean success = authService.resetPassword(resetPasswordDTO);
            if (success) {
                redirectAttributes.addFlashAttribute("reset", true);
                return "redirect:/login?reset=true";
            } else {
                model.addAttribute("errorMessage", "Mã OTP không đúng hoặc đã hết hiệu lực.");
                return "auth/reset-password";
            }
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "auth/reset-password";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "auth/access-denied";
    }
}
