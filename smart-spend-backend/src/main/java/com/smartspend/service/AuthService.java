package com.smartspend.service;

import com.smartspend.exception.DuplicateResourceException;
import com.smartspend.exception.InvalidOperationException;
import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.exception.UnauthorizedAccessException;
import com.smartspend.model.dto.auth.*;
import com.smartspend.model.dto.user.UserProfileResponse;
import com.smartspend.model.entity.PasswordResetToken;
import com.smartspend.model.entity.RefreshToken;
import com.smartspend.model.entity.User;
import com.smartspend.repository.PasswordResetTokenRepository;
import com.smartspend.repository.RefreshTokenRepository;
import com.smartspend.repository.UserRepository;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs; // 7 days default

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Password and confirm password do not match");
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new DuplicateResourceException("An account with this email address already exists");
        }

        if (userRepository.existsByUsername(request.getUsername().trim())) {
            throw new DuplicateResourceException("Username is already taken");
        }

        User user = new User(
                request.getFullName().trim(),
                request.getEmail().trim().toLowerCase(),
                request.getUsername().trim(),
                passwordEncoder.encode(request.getPassword())
        );

        User savedUser = userRepository.save(user);
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = createAndSaveRefreshToken(savedUser);

        UserProfileResponse profile = mapToProfile(savedUser);
        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessExpirationMs(), profile);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail().trim(),
                            request.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = createAndSaveRefreshToken(user);

            UserProfileResponse profile = mapToProfile(user);
            return new AuthResponse(accessToken, refreshToken, jwtService.getAccessExpirationMs(), profile);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String tokenStr = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenStr)
                .orElseThrow(() -> new UnauthorizedAccessException("Invalid refresh token"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            // Potential token reuse attack: revoke all tokens for this user
            refreshTokenRepository.revokeAllUserTokens(refreshToken.getUser().getId());
            throw new UnauthorizedAccessException("Refresh token has already been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new UnauthorizedAccessException("Refresh token has expired. Please login again.");
        }

        // Token rotation: mark old token revoked and issue new one
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = createAndSaveRefreshToken(user);

        UserProfileResponse profile = mapToProfile(user);
        return new AuthResponse(newAccessToken, newRefreshToken, jwtService.getAccessExpirationMs(), profile);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("New password and confirm password do not match");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidOperationException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all existing sessions on password change
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email address"));

        // Delete any prior reset tokens for user
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);

        PasswordResetToken resetToken = new PasswordResetToken(user, token, expiresAt);
        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Password and confirm password do not match");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(request.getToken())
                .orElseThrow(() -> new InvalidOperationException("Invalid or expired password reset token"));

        if (Boolean.TRUE.equals(resetToken.getUsed())) {
            throw new InvalidOperationException("This password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new InvalidOperationException("This password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllUserTokens(user.getId());
    }

    private String createAndSaveRefreshToken(User user) {
        String tokenStr = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000);

        RefreshToken refreshToken = new RefreshToken(user, tokenStr, expiresAt);
        refreshTokenRepository.save(refreshToken);
        return tokenStr;
    }

    private UserProfileResponse mapToProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getUsername(),
                user.getCurrency(),
                user.getCreatedAt()
        );
    }
}
