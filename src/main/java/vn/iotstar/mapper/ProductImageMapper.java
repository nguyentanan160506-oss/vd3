package vn.iotstar.mapper;

import org.springframework.stereotype.Component;
import vn.iotstar.dto.ProductImageDTO;
import vn.iotstar.entity.ProductImage;

@Component
public class ProductImageMapper {

    public ProductImageDTO toDTO(ProductImage entity) {
        if (entity == null) {
            return null;
        }
        return ProductImageDTO.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .primary(entity.getPrimary())
                .displayOrder(entity.getDisplayOrder())
                .build();
    }
}
