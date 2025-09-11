package com.example.eventproject.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Files", description = "อัปโหลดไฟล์รูปภาพ")
@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class FileController {

    @Operation(summary = "อัปโหลดไฟล์รูปภาพ (poster/detail/seatmap)", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/files", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(201).build();
    }
}
