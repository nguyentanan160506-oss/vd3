package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "iotstar_shop"
            ));
            String url = (String) uploadResult.get("secure_url");
            log.info("Cloudinary upload thành công: {}", url);
            return url;
        } catch (Exception e) {
            log.warn("Cloudinary upload thất bại ({}), thực hiện lưu trữ local dự phòng.", e.getMessage());
            return saveLocalFallback(file);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary")) {
            return;
        }
        try {
            // Lấy public_id từ url nếu cần
            String publicId = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.lastIndexOf("."));
            cloudinary.uploader().destroy("iotstar_shop/" + publicId, ObjectUtils.emptyMap());
            log.info("Đã xóa ảnh Cloudinary: {}", publicId);
        } catch (Exception e) {
            log.warn("Lỗi khi xóa ảnh Cloudinary: {}", e.getMessage());
        }
    }

    private String saveLocalFallback(MultipartFile file) {
        try {
            String uploadDir = "uploads";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);
            return "/uploads/" + fileName;
        } catch (IOException ioException) {
            log.error("Không thể lưu file local: {}", ioException.getMessage());
            return "/images/default-image.png";
        }
    }
}
