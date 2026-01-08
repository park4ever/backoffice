package com.youngwon.backoffice.domain.user;

import com.youngwon.backoffice.common.entity.BaseTimeEntity;
import com.youngwon.backoffice.domain.shop.Shop;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.youngwon.backoffice.domain.user.UserRole.*;
import static com.youngwon.backoffice.domain.user.UserStatus.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_shop_email", columnNames = {"shop_id", "email"})
        }
)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_users_shop"))
    private Shop shop;

    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // ===== 생성 로직(정적 팩토리) =====
    public static User create(Shop shop, String email, String passwordHash, String name, UserRole role) {
        User user = new User();
        user.shop = shop;
        user.email = email;
        user.passwordHash = passwordHash;
        user.name = name;
        user.role = role;
        user.status = ACTIVE;
        user.lastLoginAt = null;
        return user;
    }

    // ===== 비즈니스 메서드 =====
    public void changeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        this.name = name;
    }

    /**
     * 반드시 해시된 값만 세팅(raw password 금지)
     */
    public void changePasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("비밀번호 해시 값은 필수입니다.");
        }
        this.passwordHash = passwordHash;
    }

    public void changeRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("권한은 필수입니다.");
        }
        this.role = role;
    }

    public void activate() {
        this.status = ACTIVE;
    }

    public void deactivate() {
        this.status = INACTIVE;
    }

    public boolean isActive() {
        return this.status == ACTIVE;
    }

    public boolean isOwner() {
        return this.role == OWNER;
    }

    /**
     * 로그인 성공 시각 기록용
     */
    public void recordLoginNow() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void recordLoginAt(LocalDateTime at) {
        this.lastLoginAt = at;
    }
}