package com.youngwon.backoffice.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                //CSRF
                //백오피스 + form login 환경에서는 기본 활성화가 안전
                .csrf(Customizer.withDefaults())

                //세션 기반 인증
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                //접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                    "/login",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        //관리자 API는 ADMIN만 허용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        //그 외 요청은 인증 필요
                        .anyRequest().authenticated()
                )

                //로그인 설정(form login)
                .formLogin(form -> form
                        .loginPage("/login")            //기본 로그인 페이지 사용
                        .loginProcessingUrl("/login")   //POST 로그인 처리 URL
                        .usernameParameter("username")  //default
                        .passwordParameter("password")  //default
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )

                //로그아웃
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}