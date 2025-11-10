package com.readour.common.repository;

import com.readour.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
