package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WeChatSessionClient {
    private static final Logger log = LoggerFactory.getLogger(WeChatSessionClient.class);

    private final RestClient restClient;
    private final String appid;
    private final String secret;

    public WeChatSessionClient(
            RestClient.Builder restClientBuilder,
            @Value("${wechat.appid}") String appid,
            @Value("${wechat.secret}") String secret) {
        this.restClient = restClientBuilder.baseUrl("https://api.weixin.qq.com").build();
        this.appid = appid;
        this.secret = secret;
    }

    public String exchange(String code) {
        SessionResponse response;
        try {
            response = restClient.get()
                    .uri(builder -> builder.path("/sns/jscode2session")
                            .queryParam("appid", appid)
                            .queryParam("secret", secret)
                            .queryParam("js_code", code)
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(SessionResponse.class);
        } catch (Exception exception) {
            log.warn("WeChat session exchange unavailable");
            throw new ApiException(HttpStatus.BAD_GATEWAY, "WECHAT_UNAVAILABLE", "微信登录服务暂时不可用");
        }
        if (response == null || response.openid() == null || response.openid().isBlank()) {
            log.warn("WeChat login rejected, errorCode={}", response == null ? "empty" : response.errorCode());
            throw new ApiException(HttpStatus.UNAUTHORIZED, "WECHAT_LOGIN_FAILED", "微信登录失败");
        }
        return response.openid();
    }

    record SessionResponse(
            String openid,
            @JsonProperty("session_key") String sessionKey,
            @JsonProperty("errcode") Integer errorCode) {
    }
}
