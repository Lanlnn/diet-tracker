package com.diettracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.diettracker.config.JwtUtil;
 import com.diettracker.model.User;
 import com.diettracker.repository.UserRepository;
import java.util.LinkedHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final String WX_URL =
        "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";

    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
     private final UserRepository userRepository;

     public AuthService(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
         this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(
            new org.springframework.http.converter.StringHttpMessageConverter());
    }

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    public Map<String, Object> login(String code) {
        String url = String.format(WX_URL, appid, secret, code);
        String wxRespStr = restTemplate.getForObject(url, String.class);
        Map<String, Object> wxResp;
        try {
            wxResp = new ObjectMapper().readValue(wxRespStr, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse WeChat response", e);
        }

        log.info("WeChat response: {}", wxResp);

        if (wxResp != null && wxResp.containsKey("openid")) {
            String openid = (String) wxResp.get("openid");
            String token = jwtUtil.generateToken(openid);

             // Auto-create user if not exists
             User user = userRepository.findById(openid).orElseGet(() -> {
                 User newUser = new User();
                 newUser.setOpenid(openid);
                 return userRepository.save(newUser);
             });

             Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("openid", openid);
             result.put("nickname", user.getNickname() != null ? user.getNickname() : "");
             result.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
             return result;
        } else {
            String errMsg = "WeChat login failed";
            if (wxResp != null) {
                errMsg += ": " + wxResp.getOrDefault("errmsg", wxResp);
            }
            throw new RuntimeException(errMsg);
        }
    }
}
