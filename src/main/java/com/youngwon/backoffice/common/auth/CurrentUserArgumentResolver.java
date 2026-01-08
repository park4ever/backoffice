package com.youngwon.backoffice.common.auth;

import com.youngwon.backoffice.exception.BusinessException;
import com.youngwon.backoffice.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static com.youngwon.backoffice.common.auth.SessionKeys.LOGIN_USER;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && LoginUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        CurrentUser anno = parameter.getParameterAnnotation(CurrentUser.class);
        boolean required = (anno == null) || anno.required();

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        if (request == null) {
            if (required) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보 조회에 실패했습니다.");
            }
            return null;
        }

        //세션이 없으면 생성하지 않음(불필요한 세션 생성 방지)
        HttpSession session = request.getSession(false);
        if (session == null) {
            if (required) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
            }
            return null;
        }

        Object value = session.getAttribute(LOGIN_USER);
        if (value == null) {
            if (required) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증이 필요합니다.");
            }
            return null;
        }

        if (!(value instanceof LoginUser loginUser)) {
            //세션 키 충돌/오염 방어 : 타입이 맞지 않으면 서버 오류로 처리
            if (required) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "세션 인증 정보 형식이 올바르지 않습니다.");
            }
            return null;
        }

        return loginUser;
    }
}