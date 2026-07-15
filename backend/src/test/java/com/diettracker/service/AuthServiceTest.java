package com.diettracker.service;

import com.diettracker.dto.LoginResponse;
import com.diettracker.entity.User;
import com.diettracker.repository.UserRepository;
import com.diettracker.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock JwtUtil jwtUtil;
    @Mock UserRepository userRepository;
    @Mock WeChatSessionClient weChatSessionClient;

    @Test
    void returnsTypedSessionWithoutExposingOpenid() {
        User user = new User();
        user.setOpenid("openid-private");
        user.setNickname("林晓");
        when(weChatSessionClient.exchange("one-time-code")).thenReturn("openid-private");
        when(userRepository.findById("openid-private")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("openid-private")).thenReturn("signed-token");
        when(jwtUtil.getExpirationSeconds()).thenReturn(604800L);

        LoginResponse response = new AuthService(jwtUtil, userRepository, weChatSessionClient)
                .login("one-time-code");

        assertThat(response.token()).isEqualTo("signed-token");
        assertThat(response.expiresIn()).isEqualTo(604800L);
        assertThat(response.user().nickname()).isEqualTo("林晓");
        assertThat(response.toString()).doesNotContain("openid-private");
        verify(weChatSessionClient).exchange("one-time-code");
    }
}
