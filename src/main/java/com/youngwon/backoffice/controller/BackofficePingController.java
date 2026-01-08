package com.youngwon.backoffice.controller;

import com.youngwon.backoffice.common.auth.CurrentUser;
import com.youngwon.backoffice.common.auth.LoginUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/backoffice")
public class BackofficePingController {

    @GetMapping("/ping")
    public Map<String, Object> ping(@CurrentUser LoginUser user) {
        return Map.of(
                "userId", user.userId(),
                "shopId", user.shopId(),
                "role", user.role().name()
        );
    }
}