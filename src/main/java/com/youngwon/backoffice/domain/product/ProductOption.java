package com.youngwon.backoffice.domain.product;

import com.youngwon.backoffice.common.entity.BaseTimeEntity;
import com.youngwon.backoffice.domain.shop.Shop;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.youngwon.backoffice.domain.product.ProductOptionStatus.*;
import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(name = "product_option",
        indexes = {
                @Index(name = "idx_product_option_product_id", columnList = "product_id"),
                @Index(name = "idx_product_option_shop_id", columnList = "shop_id")
        })
public class ProductOption extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    //멀티테넌시 키로 생성 시점에만 세팅되고 이후 변경 절대 X
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName;

    @Column(name = "option_value", nullable = false, length = 100)
    private String optionValue;

    @Column(name = "price", nullable = false)
    private long price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Enumerated(STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductOptionStatus status;

    //저장 직후 optionId가 생긴 뒤에 할당하므로 initially nullable 허용
    @Column(name = "sku_key", length = 64)
    private String skuKey;

    public static ProductOption create(Product product, String optionName, String optionValue, long price, int stockQuantity) {
        validate(optionName, optionValue, price, stockQuantity);

        ProductOption option = new ProductOption();
        option.product = product;

        if (product.getShop() == null || product.getShop().getId() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "상점 정보가 없는 상품입니다.");
        }
        option.shopId = product.getShop().getId();

        option.optionName = optionName.trim();
        option.optionValue = optionValue.trim();
        option.price = price;
        option.stockQuantity = stockQuantity;
        option.status = ACTIVE;
        return option;
    }

    public void assignSkuKey(String skuKey) {
        if (skuKey == null || skuKey.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "SKU Key는 비어 있을 수 없습니다.");
        }
        if (this.skuKey != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "SKU Key는 이미 할당되어 있습니다.");
        }
        this.skuKey = skuKey;
    }

    public String getSkuLabel() {
        return optionName + "/" + optionValue;
    }

    public void changeOption(String optionName, String optionValue) {
        validateOptionText(optionName, optionValue);
        this.optionName = optionName.trim();
        this.optionValue = optionValue.trim();
    }

    public void changePrice(long price) {
        if (price < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "가격은 0보다 작을 수 없습니다.");
        }
        this.price = price;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "증가 수량은 1 이상이어야 합니다.");
        }
        this.stockQuantity += quantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "차감 수량은 1 이상이어야 합니다.");
        }
        if (this.stockQuantity - quantity < 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "재고가 부족합니다.");
        }
        this.stockQuantity -= quantity;
    }

    public void activate() {
        this.status = ACTIVE;
    }

    public void deactivate() {
        this.status = INACTIVE;
    }

    private static void validate(String optionName, String optionValue, long price, int stockQuantity) {
        validateOptionText(optionName, optionValue);
        if (price < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "가격은 0보다 작을 수 없습니다.");
        }
        if (stockQuantity < 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "재고는 0 이상이어야 합니다.");
        }
    }

    private static void validateOptionText(String optionName, String optionValue) {
        if (optionName == null || optionName.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "옵션명은 필수입니다.");
        }
        if (optionValue == null || optionValue.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "옵션값은 필수입니다.");
        }
        if (optionName.trim().length() > 100 || optionValue.trim().length() > 100) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "옵션명/옵션값은 100자를 초과할 수 없습니다.");
        }
    }
}