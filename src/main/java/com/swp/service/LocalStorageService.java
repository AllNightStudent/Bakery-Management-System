package com.swp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService {

    private static final String BASE_DIR = "C:/UploadImg"; // ✅ đường dẫn thật
    private static final String BASE_URL = "/images";       // ✅ trỏ đúng với WebConfig

    public String saveReviewImage(Long reviewId, MultipartFile file) throws IOException {
        if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Invalid image file");
        }

        String ext = Optional.ofNullable(file.getOriginalFilename())
                .filter(n -> n.contains("."))
                .map(n -> n.substring(n.lastIndexOf(".")))
                .orElse(".jpg");

        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        // Thư mục: C:/UploadImg/reviews/{reviewId}/
        Path dir = Paths.get(BASE_DIR, "reviews", reviewId.toString());
        Files.createDirectories(dir);

        Path dest = dir.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        // ✅ URL public
        return BASE_URL + "/reviews/" + reviewId + "/" + filename;
    }
}

