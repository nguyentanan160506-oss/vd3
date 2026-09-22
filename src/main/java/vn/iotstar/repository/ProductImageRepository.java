package vn.iotstar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.entity.ProductImage;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAsc(Long productId);

    long countByProductId(Long productId);
}
