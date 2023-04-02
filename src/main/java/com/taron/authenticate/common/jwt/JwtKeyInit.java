package com.taron.authenticate.common.jwt;

import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Configuration
public class JwtKeyInit {
    private final Environment env;

    @PostConstruct
    public void init() {
        JwtDTO jwtDTO = JwtDTO.getInstance();

        jwtDTO.setAccessSecretKey(Keys.hmacShaKeyFor());
        jwtDTO.setAccessTokenValidTime();
        jwtDTO.setRefreshSecretKey(Keys.hmacShaKeyFor());
        jwtDTO.setRefreshTokenValidTime();
    }
}
