package com.dahp.domain.checkin.application;

import com.dahp.domain.checkin.controller.dto.CheckInStatusResponse;
import com.dahp.domain.user.domain.User;
import com.dahp.domain.user.domain.UserRepository;
import com.dahp.domain.user.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckInService {

    private final UserRepository userRepository;

    public CheckInStatusResponse recordCheckIn(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.recordCheckIn();
        return CheckInStatusResponse.from(user);
    }

    @Transactional(readOnly = true)
    public CheckInStatusResponse status(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return CheckInStatusResponse.from(user);
    }
}
