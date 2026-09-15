package com.example.form8038cp.auth.dto;

import com.example.form8038cp.auth.entity.User;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "A valid email address is required")
    @Size(max = 75, message = "Email must not exceed 75 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Pattern(regexp = "^$|^[0-9+\\-().\\s]{7,20}$", message = "Phone number is invalid")
    private String phone;

    @NotNull(message = "Channel is required")
    private User.Channel channel;

    @AssertTrue(message = "You must accept the Terms of Service to create an account")
    private boolean termsAccepted;
}
