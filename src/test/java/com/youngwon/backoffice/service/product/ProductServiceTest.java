package com.youngwon.backoffice.service.product;

import com.youngwon.backoffice.domain.product.ProductOption;
import com.youngwon.backoffice.domain.shop.Shop;
import com.youngwon.backoffice.dto.product.ProductCreateRequest;
import com.youngwon.backoffice.dto.product.ProductOptionAddRequest;
import com.youngwon.backoffice.dto.product.ProductOptionAddResponse;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import com.youngwon.backoffice.repository.ProductOptionRepository;
import com.youngwon.backoffice.repository.ProductRepository;
import com.youngwon.backoffice.repository.ShopRepository;
import com.youngwon.backoffice.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProductServiceTest {

    @Autowired ProductRepository productRepository;
    @Autowired ProductOptionRepository productOptionRepository;
    @Autowired ShopRepository shopRepository;
    @Autowired ProductService productService;

    @Test
    @DisplayName("옵션 추가 시 SKU Key가 생성되어 저장된다.")
    void addOption_assignsSkuKey() {
        //given
        Shop shop = createShop("shop1");
        Long shopId = shop.getId();

        Long productId = productService.createProduct(shopId, new ProductCreateRequest("아메리카노"));

        //when
        ProductOptionAddResponse res = productService.addOption(
                shopId,
                productId,
                new ProductOptionAddRequest("HOT/COLD", "COLD", 4500L, 100)
        );

        //then
        assertThat(res.optionId()).isNotNull();
        assertThat(res.skuKey()).isNotBlank();

        //포맷 검증 : S{shopId}-P{productId}-O{optionId}
        assertThat(res.skuKey()).isEqualTo("S" + shopId + "-P" + productId + "-O" + res.optionId());

        ProductOption saved = productOptionRepository.findById(res.optionId()).orElseThrow();
        assertThat(saved.getSkuKey()).isEqualTo(res.skuKey());
    }

    @Test
    @DisplayName("다른 shopId로는 상품/옵션에 접근할 수 없다.")
    void tenant_isolation_by_shopId() {
        //given
        Shop shop1 = createShop("shop1");
        Shop shop2 = createShop("shop2");

        Long productId = productService.createProduct(shop1.getId(), new ProductCreateRequest("카페라떼"));
        ProductOptionAddResponse added = productService.addOption(
                shop1.getId(),
                productId,
                new ProductOptionAddRequest("HOT/COLD", "HOT", 5000L, 100)
        );

        //when & then(상품 상세 조회 차단)
        assertThatThrownBy(() -> productService.getProductDetail(shop2.getId(), productId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        //when & then(옵션 비활성화 차단)
        assertThatThrownBy(() -> productService.deactivateOption(shop2.getId(), added.optionId()))
                .isInstanceOf(BusinessException.class);
    }

    private Shop createShop(String name) {
        Shop shop = Shop.create(name);
        return shopRepository.save(shop);
    }
}