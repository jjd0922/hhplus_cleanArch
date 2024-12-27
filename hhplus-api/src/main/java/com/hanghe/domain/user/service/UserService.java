package com.hanghe.domain.user.service;

import com.hanghe.domain.user.entity.User;
import com.hanghe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /** 유저 조회*/
    public User findUserByUId(String userId) {
        return userRepository.findUserByUId(userId);
    }
}
