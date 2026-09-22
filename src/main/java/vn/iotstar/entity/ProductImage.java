package vn.iotstar.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =====================================
    // N PRODUCT IMAGE ---- 1 PRODUCT
    // =====================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false
    )
    private Product product;

    @Column(
        name = "image_url",
        nullable = false,
        length = 500
    )
    private String imageUrl;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean primary = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (primary == null) {
            primary = false;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
    }
}
