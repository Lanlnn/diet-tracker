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
 
 @RestController
 @RequestMapping("/api/upload")
 public class FileUploadController {
 
     private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);
 
     @Value("${app.upload.dir:uploads/avatars}")
     private String uploadDir;
 
     @Value("${app.base-url:https://tigercloud.asia}")
     private String baseUrl;
 
     private Path uploadPath;
 
     @PostConstruct
     public void init() {
         uploadPath = Paths.get(uploadDir);
         try {
             Files.createDirectories(uploadPath);
             log.info("Avatar upload directory: {}", uploadPath.toAbsolutePath());
         } catch (IOException e) {
             log.error("Could not create upload directory", e);
         }
     }
 
     @PostMapping("/avatar")
     public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
         if (file.isEmpty()) {
             return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
         }
 
         String originalName = file.getOriginalFilename();
         String ext = "";
         if (originalName != null && originalName.contains(".")) {
             ext = originalName.substring(originalName.lastIndexOf("."));
         }
         String newName = UUID.randomUUID().toString() + ext;
 
         try {
            byte[] bytes = file.getBytes();
            Path target = uploadPath.resolve(newName);
            Files.write(target, bytes);
             // 从 uploadDir 提取 URL 路径（如 "uploads/avatars" -> "/uploads/avatars"）
             String urlPath = "/" + uploadDir.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
             String url = baseUrl + urlPath + "/" + newName;
            log.info("Avatar uploaded: {} -> {}", originalName, url);
            return ResponseEntity.ok(Map.of("url", url));
         } catch (IOException e) {
             log.error("File upload failed", e);
             return ResponseEntity.status(500).body(Map.of("error", "Upload failed"));
         }
     }
 }
