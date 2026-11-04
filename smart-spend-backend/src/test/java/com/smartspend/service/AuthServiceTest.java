package com.smartspend.service;

import com.smartspend.exception.DuplicateResourceException;
import com.smartspend.exception.InvalidOperationException;
import com.smartspend.model.dto.auth.AuthResponse;
import com.smartspend.model.dto.auth.LoginRequest;
import com.smartspend.model.dto.auth.RefreshTokenRequest;
import com.smartspend.model.dto.auth.RegisterRequest;
import com.smartspend.model.entity.RefreshToken;
import com.smartspend.model.entity.User;
import com.smartspend.repository.PasswordResetTokenRepository;
import com.smartspend.repository.RefreshTokenRepository;
import com.smartspend.repository.UserRepository;
import com.smartspend.security.CustomUserDetails;
import com.smartspend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604800000L);
    }

    @Test
    @DisplayName("Should successfully register a new user and return tokens")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest(
                "Kartik Vairagade", "kartik@test.com", "kartik", "Password@123", "Password@123");

        when(userRepository.existsByEmail("kartik@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("kartik")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("$2a$12$hashedPassword");

        User savedUser = new User("Kartik Vairagade", "kartik@test.com", "kartik", "$2a$12$hashedPassword");
        savedUser.setId(1L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("mock-jwt-token");
        when(jwtService.getAccessExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("kartik@test.com", response.getUser().getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when password and confirm password do not match")
    void testRegisterPasswordMismatch() {
        RegisterRequest request = new RegisterRequest(
                "Kartik Vairagade", "kartik@test.com", "kartik", "Password@123", "Mismatch@123");

        assertThrows(InvalidOperationException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject duplicate email registration")
    void testRegisterDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Kartik Vairagade", "kartik@test.com", "kartik", "Password@123", "Password@123");

        when(userRepository.existsByEmail("kartik@test.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully authenticate user with valid credentials")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("kartik", "Password@123");
        User user = new User("Kartik Vairagade", "kartik@test.com", "kartik", "$2a$12$hashed");
        user.setId(1L);
        CustomUserDetails userDetails = new CustomUserDetails(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(userDetails)).thenReturn("mock-jwt-token");
        when(jwtService.getAccessExpirationMs()).thenReturn(900000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());
        assertEquals("kartik", response.getUser().getUsername());
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void testLoginInvalidCredentials() {
        LoginRequest request = new LoginRequest("kartik", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should successfully rotate refresh tokens")
    void testRefreshTokenRotation() {
        User user = new User("Kartik Vairagade", "kartik@test.com", "kartik", "hashed");
        user.setId(1L);

        RefreshToken token = new RefreshToken(user, "valid-token-str", LocalDateTime.now().plusDays(5));
        when(refreshTokenRepository.findByTokenHash("valid-token-str")).thenReturn(Optional.of(token));
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("new-jwt-token");

        RefreshTokenRequest request = new RefreshTokenRequest("valid-token-str");
        AuthResponse response = authService.refreshToken(request);

        assertNotNull(response);
        assertEquals("new-jwt-token", response.getAccessToken());
        assertTrue(token.getRevoked());
        verify(refreshTokenRepository, times(1)).save(token);
    }
}
