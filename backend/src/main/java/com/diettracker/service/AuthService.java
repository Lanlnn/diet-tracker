package com.diettracker.service;

import org.springframework.stereotype.Service;

import com.diettracker.security.JwtUtil;
import com.diettracker.dto.LoginResponse;
import com.diettracker.dto.UserProfileResponse;
import com.diettracker.entity.User;
import com.diettracker.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final WeChatSessionClient weChatSessionClient;
    private final UserGoalService userGoalService;

    public AuthService(JwtUtil jwtUtil, UserRepository userRepository, WeChatSessionClient weChatSessionClient,
                       UserGoalService userGoalService) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.weChatSessionClient = weChatSessionClient;
        this.userGoalService = userGoalService;
    }

    @Transactional
    public LoginResponse login(String code) {
        String openid = weChatSessionClient.exchange(code);
        User user = userRepository.findById(openid).orElseGet(() -> {
            User created = new User();
            created.setOpenid(openid);
            return userRepository.save(created);
        });
        return new LoginResponse(
                jwtUtil.generateToken(openid),
                jwtUtil.getExpirationSeconds(),
                UserProfileResponse.from(user, userGoalService.requireOrCreate(openid), 0));
    }
}
