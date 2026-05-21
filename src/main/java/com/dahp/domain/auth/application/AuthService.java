package com.dahp.domain.auth.application;

import com.dahp.domain.auth.controller.dto.LoginRequest;
import com.dahp.domain.auth.controller.dto.LoginResponse;
import com.dahp.domain.auth.controller.dto.SignupRequest;
import com.dahp.domain.auth.controller.dto.TokenRefreshRequest;
import com.dahp.domain.auth.exception.AuthenticationException;
import com.dahp.domain.auth.infrastructure.JwtTokenProvider;
import com.dahp.domain.user.domain.User;
import com.dahp.domain.user.domain.UserRepository;
import com.dahp.domain.user.domain.UserRole;
import com.dahp.domain.user.exception.UserNotFoundException;
import com.dahp.global.exception.BusinessException;
import com.dahp.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public User signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .checkInIntervalDays(request.checkInIntervalDays())
                .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(AuthenticationException::loginFailed);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw AuthenticationException.loginFailed();
        }
        return jwtTokenProvider.issueTokens(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(TokenRefreshRequest request) {
        Long userId = jwtTokenProvider.parseRefreshTokenUserId(request.refreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return jwtTokenProvider.issueTokens(user);
    }
}
