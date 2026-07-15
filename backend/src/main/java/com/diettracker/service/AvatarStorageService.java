package com.diettracker.service;

import com.diettracker.api.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AvatarStorageService {
    private static final Logger log = LoggerFactory.getLogger(AvatarStorageService.class);
    private static final long MAX_BYTES = 2 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    @Value("${app.upload.dir}") private String uploadDir;
    @Value("${app.base-url}") private String baseUrl;
    private Path uploadPath;

    @PostConstruct
    void init() throws IOException {
        uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) throw badRequest("文件不能为空");
        if (file.getSize() > MAX_BYTES || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw badRequest("仅支持 2MB 以内的 JPG、PNG 或 WebP 图片");
        }
        String extension = switch (file.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        Path target = uploadPath.resolve(UUID.randomUUID() + extension).normalize();
        if (!target.startsWith(uploadPath)) throw badRequest("文件名不合法");
        try {
            file.transferTo(target);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_FAILED", "头像上传失败");
        }
        return baseUrl.replaceAll("/+$", "") + "/uploads/avatars/" + target.getFileName();
    }

    public StagedDelete stageDeleteManagedAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) return null;
        String marker = "/uploads/avatars/";
        int markerIndex = avatarUrl.indexOf(marker);
        if (markerIndex < 0) return null;
        String filename = avatarUrl.substring(markerIndex + marker.length());
        if (!filename.matches("[a-fA-F0-9-]{36}\\.(jpg|png|webp)")) return null;
        Path target = uploadPath.resolve(filename).normalize();
        if (!target.startsWith(uploadPath) || !Files.exists(target)) return null;
        try {
            Path trash = uploadPath.resolve(".trash");
            Files.createDirectories(trash);
            Path staged = trash.resolve(UUID.randomUUID() + "-" + filename);
            Files.move(target, staged, StandardCopyOption.REPLACE_EXISTING);
            return new StagedDelete(target, staged);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "AVATAR_DELETE_FAILED", "头像文件清理失败，请稍后重试");
        }
    }

    public final class StagedDelete {
        private final Path original;
        private final Path staged;

        private StagedDelete(Path original, Path staged) {
            this.original = original;
            this.staged = staged;
        }

        public void commit() {
            try {
                Files.deleteIfExists(staged);
            } catch (IOException exception) {
                log.warn("Could not purge staged avatar file");
            }
        }

        public void rollback() {
            try {
                if (Files.exists(staged)) Files.move(staged, original, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                log.error("Could not restore staged avatar file after transaction rollback");
            }
        }
    }

    private ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, "INVALID_AVATAR", message);
    }
}
