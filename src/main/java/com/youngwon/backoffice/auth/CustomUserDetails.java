package com.youngwon.backoffice.auth;

import com.youngwon.backoffice.domain.user.User;
import com.youngwon.backoffice.domain.user.UserRole;
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
    private final UserRole role;
    private final UserStatus status;

    private CustomUserDetails(Long userId, Long shopId, String email, String passwordHash, UserRole role, UserStatus status) {
        this.userId = userId;
        this.shopId = shopId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.status = status;
    }

    public static CustomUserDetails from(User user) {
        //TODO : 현재 정책은 "모든 User는 Shop에 소속"임
        // - 플랫폼 운영자(ex.PLATFORM_ADMIN) 계정을 도입할 경우 :
        //  (1) users.shop_id를 nullable 허용하고 shopId를 null로 세팅하거나
        //  (2) 운영자도 전용 Shop에 귀속시키는 방식 중 하나로 정책을 확정해야함.
        return new CustomUserDetails(
                user.getId(),
                user.getShop().getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
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
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
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
        //TODO : 계정 만료 정책 도입 시, Users.account_expires_at 컬럼 추가 후 비교 처리
        // -> 예시 : 먼료일이 null이면 만료 없음, 만료일이 현재보다 과거면 false
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        //TODO : 계정 잠금 정책 도입 시, users.locked_at 또는 users.locked 컬럼 기반으로 처리
        // -> 예시 : 로그인 실패 n회 누적 시 잠금, ADMIN/OWNER 해제 기능 필요
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        //TODO : 비밀번호 만료 정책이 필요해짐녀 users.password_changed_at 컬럼 추가 후 만료 기간 체크
        // -> 예시 : 90일 경과 시 false, 비밀번호 변경 유도
        return true;
    }
    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }
}