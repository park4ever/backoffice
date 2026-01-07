package com.youngwon.backoffice.domain.product;

import com.youngwon.backoffice.common.entity.BaseTimeEntity;
import com.youngwon.backoffice.domain.shop.Shop;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.youngwon.backoffice.domain.product.ProductStatus.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "product",
        indexes = {
            @Index(name = "idx_product_shop_id", columnList = "shop_id")
        })
public class Product extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    //cascade = ALL 제거 -> 옵션은 항상 ProductOptionRepository.save()로 명시적 저장.
    @OneToMany(mappedBy = "product")
    private final List<ProductOption> options = new ArrayList<>();

    public static Product create(Shop shop, String name) {
        validateName(name);
        Product product = new Product();
        product.shop = shop;
        product.name = name.trim();
        product.status = ACTIVE;
        return product;
    }

    public void rename(String name) {
        validateName(name);
        this.name = name.trim();
    }

    public void activate() {
        this.status = ACTIVE;
    }

    public void deactivate() {
        this.status = INACTIVE;
        this.options.forEach(ProductOption::deactivate);
    }

    public ProductOption addOption(String optionName, String optionValue, long price, int stockQuantity) {
        if (this.status != ACTIVE) {
            throw new BusinessException(ErrorCode.CONFLICT, "비활성 상품에는 옵션을 추가할 수 없습니다.");
        }

        boolean duplicated = this.options.stream().anyMatch(o ->
                o.getOptionName().equals(optionName.trim()) &&
                        o.getOptionValue().equals(optionValue.trim())
        );
        if (duplicated) {
            throw new BusinessException(ErrorCode.CONFLICT, "이미 존재하는 옵션입니다.");
        }

        ProductOption option = ProductOption.create(this, optionName, optionValue, price, stockQuantity);
        this.options.add(option);
        return option;
    }

    public void removeOption(ProductOption option) {
        option.deactivate();
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상품명은 필수입니다.");
        }
        if (name.trim().length() > 200) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상품명은 200자를 초과할 수 없습니다.");
        }
    }
}