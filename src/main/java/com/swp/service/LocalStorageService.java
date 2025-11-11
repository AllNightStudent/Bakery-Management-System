package com.swp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService {

    @Value("${app.upload.root:uploads}")
    private String root;

    public String saveReviewImage(Long reviewId, MultipartFile file) throws IOException {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (ext == null) ext = "jpg";
        String safe = UUID.randomUUID().toString().replace("-", "") + "." + ext.toLowerCase();

        Path dir = Path.of(root, "reviews", String.valueOf(reviewId));
        Files.createDirectories(dir);
        Path target = dir.resolve(safe);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Static mapping: /uploads/** -> file system ./uploads/**
        return "/uploads/reviews/" + reviewId + "/" + safe;
    }
}

