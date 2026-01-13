package com.youngwon.backoffice.domain.order;

import com.youngwon.backoffice.common.entity.BaseTimeEntity;
import com.youngwon.backoffice.common.value.Money;
import com.youngwon.backoffice.domain.product.ProductOption;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.youngwon.backoffice.domain.product.ProductOptionStatus.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
@Table(
        name = "order_item",
        indexes = {
                @Index(name = "idx_order_item_shop_id_order_id", columnList = "shop_id, order_id"),
                @Index(name = "idx_order_item_shop_id_product_option_id", columnList = "shop_id, product_option_id")
        }
)
public class OrderItem extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "product_option_id", nullable = false)
    private ProductOption productOption;

    // ---- 주문 시점 스냅샷 ----
    @Column(name = "product_name_snapshot", nullable = false, length = 200)
    private String productNameSnapshot;

    @Column(name = "option_name_snapshot", nullable = false, length = 100)
    private String optionNameSnapshot;

    @Column(name = "option_value_snapshot", nullable = false, length = 100)
    private String optionValueSnapshot;

    @Column(name = "sku_key_snapshot", length = 64)
    private String skuKeySnapshot;

    // ---- 금액 스냅샷 ----
    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unit_price", nullable = false))
    private Money unitPrice;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "line_amount", nullable = false))
    private Money lineAmount;

    public static OrderItem create(Order order, ProductOption option, String productNameSnapshot, int quantity) {
        requireNonNull(order, "order");
        requireNonNull(option, "productOption");
        requireText(productNameSnapshot, "productNameSnapshot");
        requirePositive(quantity, "quantity");

        // 멀티테넌시 안전장치
        if (!order.getShopId().equals(option.getShopId())) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "주문과 옵션의 shopId가 일치하지 않습니다. orderShopId=" + order.getShopId() + ", optionShopId=" + option.getShopId()
            );
        }

        if (option.getStatus() != ACTIVE) {
            throw new BusinessException(
                    ErrorCode.CONFLICT,
                    "비활성 옵션은 주문에 포함할 수 없습니다. optionId=" + option.getId() + ", status=" + option.getStatus()
            );
        }

        OrderItem item = new OrderItem();
        item.shopId = order.getShopId();
        item.order = order;
        item.productOption = option;

        // 스냅샷 고정 (옵션/sku/가격이 나중에 바뀌어도 주문기록 불변)
        item.productNameSnapshot = productNameSnapshot;
        item.optionNameSnapshot = option.getOptionName();
        item.optionValueSnapshot = option.getOptionValue();
        item.skuKeySnapshot = option.getSkuKey();

        item.unitPrice = Money.of(option.getPrice());
        item.quantity = quantity;

        // lineAmount = unitPrice * quantity
        item.lineAmount = calculateLineAmount(item.unitPrice, quantity);

        return item;
    }

    private static Money calculateLineAmount(Money unitPrice, int quantity) {
        try {
            long line = Math.multiplyExact(unitPrice.getAmount(), (long) quantity);
            return Money.of(line);
        } catch (ArithmeticException e) {
            throw new BusinessException(
                    ErrorCode.MONEY_OVERFLOW,
                    "라인금액 계산 중 오버플로우: unitPrice = " + unitPrice.getAmount() + ", quantity = " + quantity
            );
        }
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, field + " 값이 누락되었습니다.");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, field + " 값이 누락되었습니다.");
        }
        return value;
    }

    private static void requirePositive(int value, String field) {
        if (value <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, field + "는 1 이상이어야 합니다. " + field + "=" + value);
        }
    }
}