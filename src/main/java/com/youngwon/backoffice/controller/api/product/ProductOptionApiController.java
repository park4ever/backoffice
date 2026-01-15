package com.youngwon.backoffice.controller.api.product;

import com.youngwon.backoffice.common.auth.CurrentUser;
import com.youngwon.backoffice.common.auth.LoginUser;
import com.youngwon.backoffice.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/backoffice/product-options")
public class ProductOptionApiController {

    private final ProductService productService;

    @PatchMapping("/{optionId}/deactivate")
    public ResponseEntity<Void> deactivateOption(
            @CurrentUser LoginUser user,
            @PathVariable("optionId") Long optionId
    ) {
        productService.deactivateOption(user.shopId(), optionId);
        return ResponseEntity.noContent().build();
    }
}