CREATE TABLE order_item (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shop_id BIGINT NOT NULL,

    order_id BIGINT NOT NULL,
    product_option_id BIGINT NOT NULL,

    product_name_snapshot VARCHAR(200) NOT NULL,
    option_name_snapshot VARCHAR(100) NOT NULL,
    option_value_snapshot VARCHAR(100) NOT NULL,
    sku_key_snapshot VARCHAR(64) NULL,

    unit_price BIGINT NOT NULL,
    quantity INT NOT NULL,
    line_amount BIGINT NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_order_item_order_id
        FOREIGN KEY (order_id) REFERENCES orders(id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,

    CONSTRAINT fk_order_item_product_option_id
        FOREIGN KEY (product_option_id) REFERENCES product_option(id)
        ON UPDATE RESTRICT ON DELETE RESTRICT,

    INDEX idx_order_item_shop_id_order_id (shop_id, order_id),
    INDEX idx_order_item_shop_id_product_option_id (shop_id, product_option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;