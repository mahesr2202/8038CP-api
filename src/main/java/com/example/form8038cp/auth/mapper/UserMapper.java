package com.example.form8038cp.auth.mapper;

import com.example.form8038cp.auth.dto.UserResponse;
import com.example.form8038cp.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .channel(user.getChannel())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
