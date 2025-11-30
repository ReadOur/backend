package com.readour.common.security;

import com.readour.common.entity.User;
import com.readour.common.enums.UserRole;
import com.readour.common.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final UserRole role;
    private final UserStatus status;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(Long id,
                          String email,
                          String password,
                          UserRole role,
                          UserStatus status,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.authorities = authorities;
    }

    public static UserPrincipal from(User user) {
        UserRole role = user.getRole() != null ? user.getRole() : UserRole.USER;
        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                role,
                user.getStatus(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BANNED && status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}
