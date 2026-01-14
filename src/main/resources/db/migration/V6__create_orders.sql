CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shop_id BIGINT NOT NULL,
    order_no VARCHAR(30) NOT NULL,
    sales_channel VARCHAR(20) NOT NULL,
    external_ref VARCHAR(100) NULL,
    status VARCHAR(20) NOT NULL,
    ordered_at DATETIME(6) NOT NULL,

    customer_name VARCHAR(100) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,

    gross_amount BIGINT NOT NULL,
    platform_fee_amount BIGINT NOT NULL,
    payment_fee_amount BIGINT NOT NULL,
    other_deduction_amount BIGINT NOT NULL,
    deduction_amount BIGINT NOT NULL,
    settlement_amount BIGINT NOT NULL,
    refund_amount BIGINT NOT NULL,

    memo VARCHAR(500) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_orders_shop_id_order_no UNIQUE (shop_id, order_no),
    CONSTRAINT uk_orders_shop_id_channel_external_ref UNIQUE (shop_id, sales_channel, external_ref),

    INDEX idx_orders_shop_id_ordered_at (shop_id, ordered_at),
    INDEX idx_orders_shop_id_status_ordered_at (shop_id, status, ordered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;