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
            String ext = "";
            String name = file.getOriginalFilename();
            if (name != null && name.contains(".")) {
                ext = name.substring(name.lastIndexOf('.'));
            }
            String newName = java.util.UUID.randomUUID() + ext;
            java.nio.file.Path target = java.nio.file.Paths.get(uploadDir).resolve(newName).normalize();
            java.nio.file.Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
            return "/images/" + newName;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not store file: " + e.getMessage(), e);
        }
    }

    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return false;
        try {
            String filename = extractFilename(fileUrl); // ตัด "/images/"
            if (filename == null) return false;
            java.nio.file.Path p = java.nio.file.Paths.get(uploadDir).resolve(filename).normalize();
            return java.nio.file.Files.deleteIfExists(p);
        } catch (java.io.IOException e) {
            return false;
        }
    }

    public String replaceFile(String currentUrl, MultipartFile newFile) {
        String newUrl = saveFile(newFile); // ถ้าเซฟสำเร็จค่อยลบของเก่า
        if (currentUrl != null && !currentUrl.isBlank() && !currentUrl.equals(newUrl)) {
            deleteFile(currentUrl);
        }
        return newUrl;
    }

    private String extractFilename(String fileUrl) {
        // รับทั้ง "/images/name.ext" หรือ "name.ext"
        if (fileUrl == null) return null;
        if (fileUrl.startsWith("/images/")) return fileUrl.substring("/images/".length());
        // กันเผื่อส่งเป็น URL เต็มมา เช่น http://host/images/name.ext
        int idx = fileUrl.indexOf("/images/");
        if (idx >= 0) return fileUrl.substring(idx + "/images/".length());
        // กรณีเป็นชื่อไฟล์ล้วน
        return fileUrl;
    }
}
