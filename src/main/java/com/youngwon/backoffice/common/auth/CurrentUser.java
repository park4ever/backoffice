package com.youngwon.backoffice.common.auth;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {

    /**
     * true면 로그인 세션이 없을 때 예외 발생(default).
     * false면 로그인 세션이 없을 때 null 주입(permitAll 화면 등에서 사용)
     */
    boolean required() default true;
}