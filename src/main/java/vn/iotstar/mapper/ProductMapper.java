package vn.iotstar.mapper;

import org.springframework.stereotype.Component;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductMapper {

    private final ProductImageMapper imageMapper;

    // =====================================
    // ENTITY → DTO
    // =====================================
    public ProductDTO toDTO(Product entity) {
        if (entity == null) {
            return null;
        }
        return ProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .description(entity.getDescription())
                .images(
                        entity.getImages() != null ?
                                entity.getImages()
                                        .stream()
                                        .map(imageMapper::toDTO)
                                        .collect(Collectors.toList())
                                : new ArrayList<>()
                )
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userFullName(entity.getUser() != null ? entity.getUser().getFullName() : null)
                .build();
    }

    // =====================================
    // DTO → ENTITY
    // =====================================
    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }
        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .description(dto.getDescription())
                .build();
    }

    // =====================================
    // UPDATE
    // =====================================
    public void updateEntity(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setQuantity(dto.getQuantity());
        entity.setDescription(dto.getDescription());
    }
}
