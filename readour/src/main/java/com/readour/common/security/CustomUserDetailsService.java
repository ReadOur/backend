package com.readour.common.security;

import com.readour.common.entity.User;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import com.readour.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. email=" + username));
    }

    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다. id=" + userId));
        return UserPrincipal.from(user);
    }
}
