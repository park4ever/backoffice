package com.youngwon.backoffice.controller.api.product;

import com.youngwon.backoffice.common.auth.LoginUser;
import com.youngwon.backoffice.common.auth.SessionKeys;
import com.youngwon.backoffice.domain.user.UserRole;
import com.youngwon.backoffice.dto.product.ProductDetailResponse;
import com.youngwon.backoffice.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductApiControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean ProductService productService;

    @Test
    @DisplayName("인증 없이 상품 상세 조회를 호출하면 401이 내려온다.")
    void detail_redirects_to_login_when_no_auth() throws Exception {
        mockMvc.perform(get("/api/backoffice/products/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @DisplayName("@CurrentUser 주입으로 shopId가 서비스 호출에 전달된다.")
    void detail_passes_shopId_from_session() throws Exception {
        //given
        LoginUser loginUser = new LoginUser(10L, 20L, UserRole.OWNER);

        given(productService.getProductDetail(eq(20L), eq(1L)))
                .willReturn(new ProductDetailResponse(1L, "아메리카노", "ACTIVE", List.of()));

        //when
        mockMvc.perform(get("/api/backoffice/products/1")
                        .with(user("owner").roles("OWNER"))
                        .sessionAttr(SessionKeys.LOGIN_USER, loginUser))
                .andExpect(status().isOk());

        //then
        verify(productService).getProductDetail(20L, 1L);
    }
}