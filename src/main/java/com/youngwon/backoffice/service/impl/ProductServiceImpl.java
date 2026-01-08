package com.youngwon.backoffice.service.impl;

import com.youngwon.backoffice.common.sku.SkuPolicy;
import com.youngwon.backoffice.domain.product.Product;
import com.youngwon.backoffice.domain.product.ProductOption;
import com.youngwon.backoffice.domain.shop.Shop;
import com.youngwon.backoffice.dto.product.*;
import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import com.youngwon.backoffice.repository.ProductOptionRepository;
import com.youngwon.backoffice.repository.ProductRepository;
import com.youngwon.backoffice.repository.ShopRepository;
import com.youngwon.backoffice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ShopRepository shopRepository;
    private final SkuPolicy skuPolicy;

    @Override
    public Long createProduct(Long shopId, ProductCreateRequest request) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "상점을 찾을 수 없습니다."));

        Product product = Product.create(shop, request.name());
        productRepository.save(product);

        return product.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetail(Long shopId, Long productId) {
        Product product = productRepository.findWithOptionsByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));

        List<ProductOptionSummaryResponse> options = product.getOptions().stream()
                .map(o -> new ProductOptionSummaryResponse(
                        o.getId(),
                        o.getOptionName(),
                        o.getOptionValue(),
                        o.getPrice(),
                        o.getStockQuantity(),
                        o.getStatus().name(),
                        o.getSkuKey(),
                        o.getSkuLabel()
                ))
                .toList();

        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getStatus().name(),
                options
        );
    }

    @Override
    public ProductOptionAddResponse addOption(Long shopId, Long productId, ProductOptionAddRequest request) {
        Product product = productRepository.findWithOptionsByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));

        //도메인에서 중복/상태 체크 + 옵션 생성
        ProductOption option = product.addOption(
                request.optionName(),
                request.optionValue(),
                request.price(),
                request.stockQuantity()
        );

        //옵션 저장 -> optionId 확보
        productOptionRepository.save(option);

        //SKU Key 생성/할당
        String skuKey = skuPolicy.createKey(shopId, productId, option.getId());

        option.assignSkuKey(skuKey);

        return new ProductOptionAddResponse(
                option.getId(),
                option.getSkuKey(),
                option.getSkuLabel()
        );
    }

    @Override
    public void deactivateProduct(Long shopId, Long productId) {
        Product product = productRepository.findWithOptionsByIdAndShopId(productId, shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "상품을 찾을 수 없습니다."));

        product.deactivate();
        //트랜잭션 내 더티체킹으로 반영
    }

    @Override
    public void deactivateOption(Long shopId, Long optionId) {
        ProductOption option = productOptionRepository.findByIdAndShopId(optionId, shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "상품 옵션을 찾을 수 없습니다."));

        option.deactivate();
        //트랜잭션 내 더티체킹으로 반영
    }
}