package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.ProductImage;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductImageRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.ProductService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public Page<ProductDTO> findAll(String keyword, Pageable pageable) {
        Page<Product> page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = productRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            page = productRepository.findAll(pageable);
        }
        return page.map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> findByUserId(Long userId, String keyword, Pageable pageable) {
        Page<Product> page;
        if (keyword != null && !keyword.trim().isEmpty()) {
            page = productRepository.findByUserIdAndNameContainingIgnoreCase(userId, keyword.trim(), pageable);
        } else {
            page = productRepository.findByUserId(userId, pageable);
        }
        return page.map(productMapper::toDTO);
    }

    @Override
    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
        return productMapper.toDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO dto, Long userId) {
        Product product = productMapper.toEntity(dto);
        product.setCreatedAt(LocalDateTime.now());

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            product.setUser(user);
        }

        Product saved = productRepository.save(product);

        // Xử lý upload ảnh (nếu có)
        if (dto.getImageFiles() != null && !dto.getImageFiles().isEmpty()) {
            int order = 0;
            for (MultipartFile file : dto.getImageFiles()) {
                if (file != null && !file.isEmpty()) {
                    String url = cloudinaryService.uploadImage(file);
                    if (url != null) {
                        ProductImage image = ProductImage.builder()
                                .product(saved)
                                .imageUrl(url)
                                .primary(order == 0)
                                .displayOrder(order++)
                                .createdAt(LocalDateTime.now())
                                .build();
                        productImageRepository.save(image);
                        saved.getImages().add(image);
                    }
                }
            }
        }

        return productMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        productMapper.updateEntity(dto, product);

        // Thêm ảnh mới nếu upload
        if (dto.getImageFiles() != null && !dto.getImageFiles().isEmpty()) {
            int currentOrder = product.getImages() != null ? product.getImages().size() : 0;
            for (MultipartFile file : dto.getImageFiles()) {
                if (file != null && !file.isEmpty()) {
                    String url = cloudinaryService.uploadImage(file);
                    if (url != null) {
                        ProductImage image = ProductImage.builder()
                                .product(product)
                                .imageUrl(url)
                                .primary(currentOrder == 0)
                                .displayOrder(currentOrder++)
                                .createdAt(LocalDateTime.now())
                                .build();
                        productImageRepository.save(image);
                        product.getImages().add(image);
                    }
                }
            }
        }

        Product updated = productRepository.save(product);
        return productMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        // Xóa ảnh trên Cloudinary
        if (product.getImages() != null) {
            for (ProductImage img : product.getImages()) {
                cloudinaryService.deleteImage(img.getImageUrl());
            }
        }

        productRepository.delete(product);
    }

    @Override
    public long countProducts() {
        return productRepository.count();
    }

    @Override
    public long countByUserId(Long userId) {
        return productRepository.countByUserId(userId);
    }

    @Override
    @Transactional
    public void addImage(Long productId, MultipartFile file, boolean isPrimary) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        String url = cloudinaryService.uploadImage(file);
        if (url != null) {
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageUrl(url)
                    .primary(isPrimary)
                    .displayOrder(product.getImages().size())
                    .createdAt(LocalDateTime.now())
                    .build();
            productImageRepository.save(image);
        }
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId).orElse(null);
        if (image != null) {
            cloudinaryService.deleteImage(image.getImageUrl());
            productImageRepository.delete(image);
        }
    }
}
