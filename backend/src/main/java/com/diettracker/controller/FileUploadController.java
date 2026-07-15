 package com.diettracker.controller;
 
 import jakarta.annotation.PostConstruct;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Map;
import java.util.UUID;
 import java.util.Set;
 
 @RestController
 @RequestMapping("/api/upload")
 public class FileUploadController {
 
     private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
 
     private static final long MAX_AVATAR_BYTES = 2 * 1024 * 1024;
     private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

     @Value("${app.upload.dir}")
     private String uploadDir;
 
     @Value("${app.base-url}")
     private String baseUrl;
 
     private Path uploadPath;
 
     @PostConstruct
     public void init() {
         uploadPath = Paths.get(uploadDir);
         try {
             Files.createDirectories(uploadPath);
             log.info("Avatar upload directory initialized");
         } catch (IOException e) {
             log.error("Could not create upload directory", e);
         }
     }
 
     @PostMapping("/avatar")
     public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
         if (file.isEmpty()) {
             return ResponseEntity.badRequest().body(Map.of("message", "文件不能为空"));
         }

         if (file.getSize() > MAX_AVATAR_BYTES || !ALLOWED_TYPES.contains(file.getContentType())) {
             return ResponseEntity.badRequest().body(Map.of("message", "仅支持 2MB 以内的 JPG、PNG 或 WebP 图片"));
         }
         String ext = switch (file.getContentType()) {
             case "image/png" -> ".png";
             case "image/webp" -> ".webp";
             default -> ".jpg";
         };
         String newName = UUID.randomUUID().toString() + ext;
 
         try {
            byte[] bytes = file.getBytes();
            Path target = uploadPath.resolve(newName).normalize();
            if (!target.startsWith(uploadPath.normalize())) {
                return ResponseEntity.badRequest().body(Map.of("message", "文件名不合法"));
            }
            Files.write(target, bytes);
             String url = baseUrl.replaceAll("/+$", "") + "/uploads/avatars/" + newName;
            log.info("Avatar uploaded successfully");
            return ResponseEntity.ok(Map.of("url", url));
         } catch (IOException e) {
             log.error("File upload failed", e);
             return ResponseEntity.status(500).body(Map.of("error", "Upload failed"));
         }
     }
 }
