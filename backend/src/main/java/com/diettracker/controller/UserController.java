 package com.diettracker.controller;
 
 import com.diettracker.model.User;
 import com.diettracker.repository.UserRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.Map;
 
 @RestController
 @RequestMapping("/api/auth")
 public class UserController {
 
     private static final Logger log = LoggerFactory.getLogger(UserController.class);
 
     private final UserRepository userRepository;
 
     public UserController(UserRepository userRepository) {
         this.userRepository = userRepository;
     }
 
     @GetMapping("/profile")
     public ResponseEntity<?> getProfile(@RequestAttribute("userId") String userId) {
         User user = userRepository.findById(userId).orElse(null);
         if (user == null) {
             return ResponseEntity.ok(Map.of(
                 "openid", userId,
                 "nickname", "",
                 "avatarUrl", ""
             ));
         }
         return ResponseEntity.ok(Map.of(
             "openid", user.getOpenid(),
             "nickname", user.getNickname() != null ? user.getNickname() : "",
             "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
         ));
     }
 
     @PutMapping("/profile")
     public ResponseEntity<?> updateProfile(
             @RequestAttribute("userId") String userId,
             @RequestBody Map<String, String> body) {
         
         String nickname = body.get("nickname");
         String avatarUrl = body.get("avatarUrl");
         
         User user = userRepository.findById(userId).orElseGet(() -> {
             User newUser = new User();
             newUser.setOpenid(userId);
             return newUser;
         });
         
         if (nickname != null && !nickname.trim().isEmpty()) {
             user.setNickname(nickname.trim());
         }
         if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
             user.setAvatarUrl(avatarUrl.trim());
         }
         
         userRepository.save(user);
         log.info("Profile updated for user {}: nickname={}, avatarUrl={}", userId, nickname, avatarUrl);
         
         return ResponseEntity.ok(Map.of(
             "openid", user.getOpenid(),
             "nickname", user.getNickname() != null ? user.getNickname() : "",
             "avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
         ));
     }
 }
