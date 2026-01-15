package com.youngwon.backoffice.controller.api.product;

import com.youngwon.backoffice.common.auth.CurrentUser;
import com.youngwon.backoffice.common.auth.LoginUser;
import com.youngwon.backoffice.dto.product.ProductCreateRequest;
import com.youngwon.backoffice.dto.product.ProductDetailResponse;
import com.youngwon.backoffice.dto.product.ProductOptionAddRequest;
import com.youngwon.backoffice.dto.product.ProductOptionAddResponse;
import com.youngwon.backoffice.service.product.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backoffice/products")
public class ProductApiController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> create(
            @CurrentUser LoginUser user,
            @RequestBody @Valid ProductCreateRequest request
    ) {
        Long productId = productService.createProduct(user.shopId(), request);

        return ResponseEntity
                .created(URI.create("/api/backoffice/products/" + productId))
                .body(Map.of("productId", productId));
    }

    @GetMapping("/{productId}")
    public ProductDetailResponse detail(
            @CurrentUser LoginUser user,
            @PathVariable("productId") Long productId
    ) {
        return productService.getProductDetail(user.shopId(), productId);
    }

    @PostMapping("/{productId}/options")
    public ResponseEntity<ProductOptionAddResponse> addOption(
            @CurrentUser LoginUser user,
            @PathVariable("productId") Long productId,
            @RequestBody @Valid ProductOptionAddRequest request
    ) {
        ProductOptionAddResponse response = productService.addOption(user.shopId(), productId, request);

        return ResponseEntity
                .created(URI.create("/api/backoffice/product-options/" + response.optionId()))
                .body(response);
    }

    @PatchMapping("/{productId}/deactivate")
    public ResponseEntity<Void> deactivateProduct(
            @CurrentUser LoginUser user,
            @PathVariable("productId") Long productId
    ) {
        productService.deactivateProduct(user.shopId(), productId);
        return ResponseEntity.noContent().build();
    }
}