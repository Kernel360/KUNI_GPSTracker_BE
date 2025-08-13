package com.example.BackendServer.user.service;

import com.example.BackendServer.global.exception.CustomException;
import com.example.BackendServer.global.exception.ErrorCode;
import com.example.BackendServer.user.db.UserEntity;
import com.example.BackendServer.user.db.UserRepository;
import com.example.BackendServer.user.db.UserRole;
import com.example.BackendServer.global.jwt.JwtUtil;
import com.example.BackendServer.user.model.request.*;
import com.example.BackendServer.user.model.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        var user = userRepository.findById(id)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getRole() == null) {
            throw new CustomException(ErrorCode.INVALID_ROLE);
        }

        return User.builder()
            .username(user.getId())
            .password(user.getPassword())
            .authorities("ROLE_" + user.getRole().name())
            .build();
    }


}
