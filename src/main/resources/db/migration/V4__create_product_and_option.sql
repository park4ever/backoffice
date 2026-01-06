-- Product, ProductOption 초기 테이블 생성
-- 기준 엔티티:
--  - Product (shop_id FK, name VARCHAR(200), status VARCHAR(20), BaseTimeEntity timestamps)
--  - ProductOption (shop_id(denormalized), product_id FK, option_name/value, price BIGINT, stock_quantity INT, status VARCHAR(20), sku_key VARCHAR(64) NULL)
-- TODO : sku_key를 "항상 생성" 흐름이 안정화되면 NOT NULL로 승격하는 V5 마이그레이션 추가 권장
-- TODO : 상품/옵션 조회가 커지면 (shop_id, id) 복합 인덱스 등 조회 패턴 기반으로 인덱스 튜닝

CREATE TABLE IF NOT EXISTS product (
    id BIGINT NOT NULL AUTO_INCREMENT,
    shop_id BIGINT NOT NULL,

    name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),
    INDEX idx_product_shop_id (shop_id),

    CONSTRAINT fk_product_shop
        FOREIGN KEY (shop_id) REFERENCES shop(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- TODO : product.status 값이 enum 문자열(예: ACTIVE/INACTIVE)로 저장되는지 확인.
--   - @Enumerated(STRING) 이므로 엔티티 enum 이름과 동일하게 들어감. enum 변경 시 데이터 영향 있음.


CREATE TABLE IF NOT EXISTS product_option (
    id BIGINT NOT NULL AUTO_INCREMENT,

    -- denormalized tenant key (멀티테넌시 / 유니크 제약 / 조회 최적화 목적)
    shop_id BIGINT NOT NULL,

    product_id BIGINT NOT NULL,

    option_name VARCHAR(100) NOT NULL,
    option_value VARCHAR(100) NOT NULL,

    price BIGINT NOT NULL,
    stock_quantity INT NOT NULL,

    status VARCHAR(20) NOT NULL,

    -- 저장 후 optionId 확보 → skuKey 부여 흐름이므로 초기에는 NULL 허용
    sku_key VARCHAR(64) NULL,

    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    INDEX idx_product_option_shop_id (shop_id),
    INDEX idx_product_option_product_id (product_id),
    INDEX idx_product_option_shop_product (shop_id, product_id),

    -- 샵 단위 유니크 (MySQL에서 NULL은 중복 허용)
    UNIQUE KEY uq_product_option_shop_sku_key (shop_id, sku_key),

    CONSTRAINT fk_product_option_shop
        FOREIGN KEY (shop_id) REFERENCES shop(id),

    CONSTRAINT fk_product_option_product
        FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

-- TODO : "옵션 생성 유스케이스" 구현 완료 후 sku_key NOT NULL 승격 마이그레이션 추가
-- 예) V5__product_option_sku_key_not_null.sql
--   ALTER TABLE product_option MODIFY COLUMN sku_key VARCHAR(64) NOT NULL;

-- TODO : 재고 차감/복구가 많아지면 product_option 조회가 (shop_id, id) 또는 (shop_id, product_id)로 많이 나갈 수 있음
--   - 필요 시 INDEX (shop_id, product_id) 추가 검토
