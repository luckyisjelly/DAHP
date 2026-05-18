package com.dahp.domain.user.application;

import com.dahp.domain.user.controller.dto.UpdateUserRequest;
import com.dahp.domain.user.controller.dto.UserResponse;
import com.dahp.domain.user.domain.User;
import com.dahp.domain.user.domain.UserRepository;
import com.dahp.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return UserResponse.from(user);
    }

    public UserResponse updateMe(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        if (request.checkInIntervalDays() != null) {
            user.updateCheckInInterval(request.checkInIntervalDays());
        }
        return UserResponse.from(user);
    }
}
