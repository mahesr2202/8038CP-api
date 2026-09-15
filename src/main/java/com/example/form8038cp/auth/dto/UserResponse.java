package com.example.form8038cp.auth.dto;

import com.example.form8038cp.auth.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private User.Role role;
    private User.Status status;
    private User.Channel channel;
    private boolean emailVerified;
}
