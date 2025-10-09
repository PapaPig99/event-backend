package com.example.eventproject.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    @TempDir
    Path tempDir; // โฟลเดอร์ชั่วคราวสำหรับแต่ละเทสต์

    private FileStorageService newServiceWithUploadDir(Path dir) {
        FileStorageService s = new FileStorageService();
        try {
            Field f = FileStorageService.class.getDeclaredField("uploadDir");
            f.setAccessible(true);
            f.set(s, dir.toString());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return s;
    }

    private MultipartFile nonEmptyMock(String originalName) throws IOException {
        MultipartFile file = mock(MultipartFile.class, Answers.RETURNS_DEEP_STUBS);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(originalName);
        // ให้ transferTo สร้างไฟล์จริง เพื่อเช็คภายหลังได้
        doAnswer(inv -> {
            File target = inv.getArgument(0);
            Files.createDirectories(target.toPath().getParent());
            Files.createFile(target.toPath());
            return null;
        }).when(file).transferTo(any(File.class));
        return file;
    }

    private MultipartFile emptyMock() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        return file;
    }

    // ---------- saveFile ----------

    @Test
    @DisplayName("saveFile(null/empty) → คืน null และไม่เรียก transferTo")
    void saveFile_null_or_empty() throws Exception {
        FileStorageService s = newServiceWithUploadDir(tempDir);

        assertNull(s.saveFile(null));

        MultipartFile empty = emptyMock();
        assertNull(s.saveFile(empty));
        verify(empty, never()).transferTo(any(File.class));
    }

    @Test
    @DisplayName("saveFile: มีนามสกุลไฟล์ → สร้างไฟล์จริงและคืน URL /images/<uuid>.ext")
    void saveFile_with_extension() throws Exception {
        FileStorageService s = newServiceWithUploadDir(tempDir);
        MultipartFile f = nonEmptyMock("photo.png");

        String url = s.saveFile(f);

        assertNotNull(url);
        assertTrue(url.startsWith("/images/"));
        assertTrue(url.endsWith(".png"));

        String filename = url.substring("/images/".length());
        assertTrue(Files.exists(tempDir.resolve(filename)));
        verify(f, times(1)).transferTo(any(File.class));
    }

    @Test
    @DisplayName("saveFile: ไม่มีนามสกุลไฟล์ → คืน URL /images/<uuid> (ไม่มี .ext) และไฟล์ถูกสร้าง")
    void saveFile_without_extension() throws Exception {
        FileStorageService s = newServiceWithUploadDir(tempDir);
        MultipartFile f = nonEmptyMock("noext");

        String url = s.saveFile(f);

        assertNotNull(url);
        assertTrue(url.startsWith("/images/"));

        String filename = url.substring("/images/".length());
        assertFalse(filename.endsWith(".")); // ไม่มีจุดท้ายชื่อ
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    // ---------- deleteFile ----------

    @Test
    @DisplayName("deleteFile: ลบไฟล์ /images/<name> สำเร็จ → true")
    void deleteFile_basic() throws Exception {
        FileStorageService s = newServiceWithUploadDir(tempDir);

        // สร้างไฟล์ทดสอบ
        Path p = tempDir.resolve("abc.jpg");
        Files.createFile(p);
        assertTrue(Files.exists(p));

        boolean ok = s.deleteFile("/images/abc.jpg");
        assertTrue(ok);
        assertFalse(Files.exists(p));
    }

    @Test
    @DisplayName("deleteFile: URL แบบ full path ที่มี /images/ แทรกอยู่ → ลบได้")
    void deleteFile_fullUrlContainingImages() throws Exception {
        FileStorageService s = newServiceWithUploadDir(tempDir);

        Path p = tempDir.resolve("xyz.dat");
        Files.createFile(p);
        assertTrue(Files.exists(p));

        boolean ok = s.deleteFile("https://cdn.example.com/static/images/xyz.dat");
        assertTrue(ok);
        assertFalse(Files.exists(p));
    }

    @Test
    @DisplayName("deleteFile: ไฟล์ไม่พบ / พารามิเตอร์ null/blank → false")
    void deleteFile_notfound_or_blank() {
        FileStorageService s = newServiceWithUploadDir(tempDir);

        assertFalse(s.deleteFile("/images/notfound.png"));
        assertFalse(s.deleteFile(null));
        assertFalse(s.deleteFile("   "));
    }

    // ---------- replaceFile ----------

    @Test
    @DisplayName("replaceFile: มีไฟล์ใหม่ → saveFile + ลบไฟล์เก่า (เมื่อชื่อไม่เท่ากัน)")
    void replaceFile_replaces_and_deletes_old() throws Exception {
        FileStorageService s = newServiceWithUploadDir(tempDir);

        // สร้างไฟล์เก่า
        Path old = tempDir.resolve("old.jpg");
        Files.createFile(old);
        String oldUrl = "/images/old.jpg";
        assertTrue(Files.exists(old));

        // ไฟล์ใหม่
        MultipartFile newFile = nonEmptyMock("new.jpg");

        String newUrl = s.replaceFile(oldUrl, newFile);
        assertNotNull(newUrl);
        assertTrue(newUrl.startsWith("/images/"));

        // ไฟล์ใหม่ต้องมีอยู่
        Path newPath = tempDir.resolve(newUrl.substring("/images/".length()));
        assertTrue(Files.exists(newPath));

        // ไฟล์เก่าต้องถูกลบ
        assertFalse(Files.exists(old));
    }

    @Test
    @DisplayName("replaceFile: newFile เป็น null/empty → จะลบไฟล์เดิม และคืน null")
    void replaceFile_delete_old_when_new_null() throws Exception {
        FileStorageService s = newServiceWithUploadDir(tempDir);

        // สร้างไฟล์เก่า
        Path old = tempDir.resolve("keepme.png");
        Files.createFile(old);
        String oldUrl = "/images/keepme.png";
        assertTrue(Files.exists(old));

        // newFile = null
        String result1 = s.replaceFile(oldUrl, null);
        assertNull(result1);
        assertFalse(Files.exists(old)); // ถูกลบทิ้ง

        // สร้างใหม่อีกครั้ง แล้วลอง newFile empty
        Path old2 = tempDir.resolve("again.png");
        Files.createFile(old2);
        String oldUrl2 = "/images/again.png";
        MultipartFile empty = emptyMock();

        String result2 = s.replaceFile(oldUrl2, empty);
        assertNull(result2);
        assertFalse(Files.exists(old2)); // ถูกลบทิ้งเช่นกัน
    }

    // ---------- saveFile: IOException pathway ----------

    @Test
    @DisplayName("saveFile: ถ้า transferTo โยน IOException → โยน RuntimeException ที่มีข้อความอธิบาย")
    void saveFile_transfer_ioexception() throws Exception {
        FileStorageService s = newServiceWithUploadDir(tempDir);

        MultipartFile f = mock(MultipartFile.class, Answers.RETURNS_DEEP_STUBS);
        when(f.isEmpty()).thenReturn(false);
        when(f.getOriginalFilename()).thenReturn("boom.pdf");
        doThrow(new IOException("disk full")).when(f).transferTo(any(File.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> s.saveFile(f));
        assertTrue(ex.getMessage().contains("Could not store file"));
        assertNotNull(ex.getCause());
        assertEquals("disk full", ex.getCause().getMessage());
    }
}
