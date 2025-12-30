package com.youngwon.backoffice.domain.shop;

import com.youngwon.backoffice.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.youngwon.backoffice.domain.shop.ShopStatus.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "shop")
public class Shop extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShopStatus status;

    // ===== 생성 로직(정적 팩토리) =====
    public static Shop create(String name) {
        Shop shop = new Shop();
        shop.name = name;
        shop.status = ACTIVE;
        return shop;
    }

    // ===== 비즈니스 메서드 =====
    public void rename(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("상점명은 필수입니다.");
        }
        this.name = name;
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
}