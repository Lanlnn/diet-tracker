package com.diettracker.controller;

import com.diettracker.service.AvatarStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {
    private final AvatarStorageService storage;

    public FileUploadController(AvatarStorageService storage) {
        this.storage = storage;
    }

    @PostMapping("/avatar")
    public Map<String, String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Map.of("url", storage.store(file));
    }
}
