package com.diettracker.admin;

import com.diettracker.api.ApiException;
import com.diettracker.entity.User;
import com.diettracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class AdminUserRefService {
    private static final Pattern SUPPORT_REF = Pattern.compile("usr_[a-f0-9]{32}");
    private final UserRepository users;

    public AdminUserRefService(UserRepository users) { this.users = users; }

    public String reference(String userId) {
        return users.findById(userId).map(User::getSupportRef)
                .orElseThrow(() -> notFound());
    }

    public User resolve(String supportRef) {
        if (!isValid(supportRef)) throw notFound();
        return users.findBySupportRef(supportRef).orElseThrow(this::notFound);
    }

    public boolean isValid(String supportRef) {
        return supportRef != null && SUPPORT_REF.matcher(supportRef).matches();
    }

    private ApiException notFound() {
        return new ApiException(HttpStatus.NOT_FOUND, "SUPPORT_USER_NOT_FOUND", "未找到该脱敏用户编号");
    }
}
