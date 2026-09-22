package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(nullable = false, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type", nullable = false, length = 30)
    private OtpType otpType;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (expiryTime == null) {
            expiryTime = LocalDateTime.now().plusMinutes(5);
        }
    }
}
