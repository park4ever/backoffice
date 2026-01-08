package com.youngwon.backoffice.common.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.youngwon.backoffice.common.auth.SessionKeys.LOGIN_USER;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            //예상치 못한 principal 타입이면 세션 저장 없이 기본 성공 흐름으로 진행
            response.sendRedirect("/");
            return;
        }

        //세션 생성/획득
        HttpSession session = request.getSession(true);

        LoginUser loginUser = new LoginUser(
                userDetails.getUserId(),
                userDetails.getShopId(),
                userDetails.getRole()
        );

        session.setAttribute(LOGIN_USER, loginUser);

        //TODO : 원래 가려던 페이지로 보내기.
        response.sendRedirect("/");
    }
}