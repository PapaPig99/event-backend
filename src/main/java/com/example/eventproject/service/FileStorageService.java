package com.example.eventproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    @Value("${app.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String orig = file.getOriginalFilename();
            String ext = (orig != null && orig.contains(".")) ? orig.substring(orig.lastIndexOf('.')) : "";
            String newName = java.util.UUID.randomUUID() + ext;

            // ชัวร์เรื่องพาธสัมพัทธ์/นอร์มัลไลซ์
            java.nio.file.Path root = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();
            java.nio.file.Files.createDirectories(root);
            java.nio.file.Path target = root.resolve(newName);

            file.transferTo(target.toFile());

            // URL ที่ frontend ควรใช้
            return "/images/" + newName;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not store file: " + e.getMessage(), e);
        }
    }

    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return false;
        try {
            String filename = extractFilename(fileUrl);
            if (filename == null) return false;
            java.nio.file.Path root = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();
            return java.nio.file.Files.deleteIfExists(root.resolve(filename));
        } catch (java.io.IOException e) {
            return false;
        }
    }

    public String replaceFile(String currentUrl, MultipartFile newFile) {
        String newUrl = saveFile(newFile);
        if (currentUrl != null && !currentUrl.isBlank() && !currentUrl.equals(newUrl)) {
            deleteFile(currentUrl);
        }
        return newUrl;
    }

    private String extractFilename(String fileUrl) {
        if (fileUrl == null) return null;
        if (fileUrl.startsWith("/images/")) return fileUrl.substring("/images/".length());
        int idx = fileUrl.indexOf("/images/");
        return (idx >= 0) ? fileUrl.substring(idx + "/images/".length()) : fileUrl;
    }
}

