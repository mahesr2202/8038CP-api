package com.example.form8038cp.auth.controller;

import com.example.form8038cp.auth.dto.ChangePasswordRequest;
import com.example.form8038cp.auth.dto.EmailVerificationChallengeResponse;
import com.example.form8038cp.auth.dto.ForgotPasswordRequest;
import com.example.form8038cp.auth.dto.LoginOutcome;
import com.example.form8038cp.auth.dto.LoginRequest;
import com.example.form8038cp.auth.dto.MessageResponse;
import com.example.form8038cp.auth.dto.RefreshResponse;
import com.example.form8038cp.auth.dto.ResendEmailOtpRequest;
import com.example.form8038cp.auth.dto.ResetPasswordRequest;
import com.example.form8038cp.auth.dto.SignupRequest;
import com.example.form8038cp.auth.dto.VerifyEmailOtpRequest;
import com.example.form8038cp.auth.service.AuthService;
import com.example.form8038cp.exception.ErrorResponse;
import com.example.form8038cp.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Signup, login, logout, token refresh, and password management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Create an account",
            description = "Registers a new (unverified) account and emails a 6-digit OTP. "
                    + "Returns a challengeId for POST /auth/verify-email — never a JWT directly. "
                    + "The `channel` field identifies the originating integration surface.",
            security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created — verify email next"),
            @ApiResponse(responseCode = "409", description = "Email already registered",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<EmailVerificationChallengeResponse> signup(
            @Valid @RequestBody SignupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
    }

    @Operation(summary = "Log in",
            description = "Authenticates and returns a JWT, or (if email is unverified) re-sends "
                    + "the OTP and returns a verification challenge. Check for 'token' in the body "
                    + "to distinguish the two outcomes.",
            security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated, or verification challenge issued"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginOutcome> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Verify email OTP",
            description = "Consumes the 6-digit code emailed at signup or login and marks the account verified. "
                    + "Does not return a token — sign in afterward with POST /auth/login.",
            security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified — sign in now"),
            @ApiResponse(responseCode = "410", description = "Code expired, used, or max attempts exceeded",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(
            @Valid @RequestBody VerifyEmailOtpRequest request) {
        authService.verifyEmailOtp(request.getChallengeId(), request.getCode());
        return ResponseEntity.ok(new MessageResponse("Your email is verified. You can now sign in."));
    }

    @Operation(summary = "Resend verification OTP",
            description = "Sends a fresh 6-digit code for an in-progress email-verification challenge.",
            security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New code emailed"),
            @ApiResponse(responseCode = "410", description = "Verification session expired or already used",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/verify-email/resend")
    public ResponseEntity<EmailVerificationChallengeResponse> resendVerifyEmail(
            @Valid @RequestBody ResendEmailOtpRequest request) {
        return ResponseEntity.ok(authService.resendEmailOtp(request.getChallengeId()));
    }

    @Operation(summary = "Log out",
            description = "Stateless JWT — no server-side state is invalidated. "
                    + "The caller is expected to discard the token. Requires a valid bearer token.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logged out"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Refresh bearer token",
            description = "Issues a new, freshly-expiring token for the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New token issued"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh() {
        return ResponseEntity.ok(authService.refresh());
    }

    @Operation(summary = "Request a password reset email",
            description = "Always returns the same message whether or not the email is registered, "
                    + "to avoid leaking which emails have accounts.",
            security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request accepted"),
            @ApiResponse(responseCode = "422", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(
                new MessageResponse("If an account exists for that email, we've sent a reset link."));
    }

    @Operation(summary = "Reset password with a token",
            description = "Consumes the single-use token from the reset email and sets a new password. "
                    + "Invalidates all existing sessions.",
            security = {})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password updated"),
            @ApiResponse(responseCode = "410", description = "Reset link expired or already used",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(new MessageResponse("Your password has been updated."));
    }

    @Operation(summary = "Change password",
            description = "Changes password for the currently authenticated user. Requires current password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Current password incorrect or new matches old",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(SecurityUtil.currentUserId(), request);
        return ResponseEntity.ok(new MessageResponse("Your password has been changed successfully."));
    }
}
