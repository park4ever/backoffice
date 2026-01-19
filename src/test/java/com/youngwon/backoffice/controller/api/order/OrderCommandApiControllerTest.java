package com.youngwon.backoffice.controller.api.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youngwon.backoffice.common.auth.CurrentUser;
import com.youngwon.backoffice.common.auth.LoginUser;
import com.youngwon.backoffice.common.value.Money;
import com.youngwon.backoffice.domain.order.SalesChannel;
import com.youngwon.backoffice.domain.user.UserRole;
import com.youngwon.backoffice.dto.order.create.OrderCreateItemRequest;
import com.youngwon.backoffice.dto.order.create.OrderCreateRequest;
import com.youngwon.backoffice.service.order.OrderCommandService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;

import static com.youngwon.backoffice.common.auth.SessionKeys.LOGIN_USER;
import static org.junit.jupiter.api.extension.MediaType.APPLICATION_JSON;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderCommandApiController.class)
@Import(OrderCommandApiControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderCommandApiControllerTest {

    @MockitoBean OrderCommandService orderCommandService;

    @Resource MockMvc mockMvc;

    @Resource ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/orders - 주문 생성 성공")
    void create_ok() throws Exception {
        // given
        given(orderCommandService.create(any())).willReturn(1L);

        OrderCreateRequest req = new OrderCreateRequest(
                "20260119-000001",
                SalesChannel.MANUAL,
                null,
                LocalDateTime.of(2026, 1, 19, 10, 0),
                "홍길동",
                "01011112222",
                Money.of(3000L),
                Money.zero(),
                Money.zero(),
                Money.zero(),
                "memo",
                List.of(new OrderCreateItemRequest(10L, 2))
        );

        // when & then
        mockMvc.perform(post("/api/orders")
                        .sessionAttr(LOGIN_USER, new LoginUser(1L, 1L, UserRole.OWNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.orderId").value(1L));

        verify(orderCommandService).create(any());
    }

    @Test
    @DisplayName("POST /api/orders - items가 비어있으면 400")
    void create_itemsEmpty_400() throws Exception {
        OrderCreateRequest req = new OrderCreateRequest(
                "20260119-000002",
                SalesChannel.MANUAL,
                null,
                LocalDateTime.of(2026, 1, 19, 10, 0),
                "홍길동",
                "01011112222",
                Money.of(3000L),
                Money.zero(),
                Money.zero(),
                Money.zero(),
                null,
                List.of() // invalid: @Size(min=1)
        );

        mockMvc.perform(post("/api/orders")
                        .sessionAttr(LOGIN_USER, new LoginUser(1L, 1L, UserRole.OWNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/orders/{id}/confirm - shopId와 orderId로 서비스 호출")
    void confirm_ok() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/confirm", 5L)
                        .sessionAttr(LOGIN_USER, new LoginUser(1L, 1L, UserRole.OWNER)))
                .andExpect(status().isOk());

        verify(orderCommandService).confirm(eq(1L), eq(5L)); // test resolver가 shopId=1L 주입
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel - shopId와 orderId로 서비스 호출")
    void cancel_ok() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/cancel", 7L)
                        .sessionAttr(LOGIN_USER, new LoginUser(1L, 1L, UserRole.OWNER)))
                .andExpect(status().isOk());

        verify(orderCommandService).cancel(eq(1L), eq(7L));
    }

    // ---- 테스트에서 @CurrentUser LoginUser 주입을 위한 설정 ----
    static class TestConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(CurrentUser.class)
                            && parameter.getParameterType().equals(LoginUser.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter,
                                              ModelAndViewContainer mavContainer,
                                              NativeWebRequest webRequest,
                                              WebDataBinderFactory binderFactory) {
                    // 테스트 고정 유저
                    return new LoginUser(1L, 1L, UserRole.OWNER);
                }
            });
        }
    }
}