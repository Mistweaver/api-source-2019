package com.api.demo.security.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityConstants {
    public static final String SECRET = "########";
    public static final long EXPIRATION_TIME = 28800000; // 8 hour
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String APP_NAME = "demo-api";
}
