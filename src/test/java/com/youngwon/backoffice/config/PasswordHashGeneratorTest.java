package com.youngwon.backoffice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGeneratorTest {
    
    @Test
    void generate() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "password";
        String hash = encoder.encode(raw);

        System.out.println("RAW = " + raw);
        System.out.println("BCRYPT = " + hash);
        System.out.println("WITH_PREFIX = {bcrypt}" + hash);
    }
}