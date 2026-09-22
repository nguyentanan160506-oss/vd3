package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;

public interface ProductService {
    Page<ProductDTO> findAll(String keyword, Pageable pageable);
    Page<ProductDTO> findByUserId(Long userId, String keyword, Pageable pageable);
    ProductDTO findById(Long id);
    ProductDTO createProduct(ProductDTO dto, Long userId);
    ProductDTO updateProduct(Long id, ProductDTO dto);
    void deleteProduct(Long id);
    long countProducts();
    long countByUserId(Long userId);
    void addImage(Long productId, MultipartFile file, boolean isPrimary);
    void deleteImage(Long imageId);
}
