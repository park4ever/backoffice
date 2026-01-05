package com.youngwon.backoffice.auth;

import com.youngwon.backoffice.domain.user.User;
import com.youngwon.backoffice.domain.user.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final Long shopId;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final UserStatus status;

    private CustomUserDetails(Long userId, Long shopId, String email, String passwordHash, String role, UserStatus status) {
        this.userId = userId;
        this.shopId = shopId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }

    public static CustomUserDetails from(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getShop().getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole().name(),
                user.getStatus()
        );
    }

    public Long getUserId() {
        return userId;
    }
    public Long getShopId() {
        return shopId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getPassword() {
        return passwordHash;
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
        return true;
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