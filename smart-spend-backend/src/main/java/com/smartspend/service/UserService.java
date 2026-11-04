package com.smartspend.service;

import com.smartspend.exception.ResourceNotFoundException;
import com.smartspend.model.dto.user.UpdateProfileRequest;
import com.smartspend.model.dto.user.UserProfileResponse;
import com.smartspend.model.entity.User;
import com.smartspend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        User user = getUserOrThrow(userId);
        return mapToProfile(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserOrThrow(userId);
        user.setFullName(request.getFullName().trim());
        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            user.setCurrency(request.getCurrency().trim().toUpperCase());
        }
        User saved = userRepository.save(user);
        return mapToProfile(saved);
    }

    public User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
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
