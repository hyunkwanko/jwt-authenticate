package com.taron.authenticate.config;

import com.taron.authenticate.common.jwt.JwtSecurityConfig;
import com.taron.authenticate.common.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Arrays;
import java.util.Objects;

@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final Environment env;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // Filter 한글 깨짐 방지
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);

        httpSecurity
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource())

                .and()
                .headers()
                .frameOptions()
                .sameOrigin()

                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeHttpRequests()
                    .antMatchers("/api/v1/member/signUp").permitAll() // 회원가입
                    .antMatchers("/api/v1/auth/login").permitAll() // 로그인
                    .antMatchers("/api/v1/auth/checkDuplicateEmail").permitAll() // 이메일 중복 체크
                    .antMatchers("/api/v1/auth/checkDuplicateNickname").permitAll() // 닉네임 중복 체크
                    .antMatchers("/api/v1/auth/sendEmailFindPassword").permitAll() // 비밀번호 찾기 이메일 발송
                    .antMatchers("/api/v1/auth/updatePassword").permitAll() // 비밀번호 재설정
                    .antMatchers("/api/v1/auth/getAccessToken").permitAll() // Access Token 재발급

                    // Swagger
                    .antMatchers("/swagger-resources/**").permitAll()
                    .antMatchers("/swagger-ui/**").permitAll()
                    .antMatchers("/api-docs/**").permitAll()
                .anyRequest().authenticated()

                .and()
                .addFilterBefore(characterEncodingFilter, CsrfFilter.class)
                .apply(new JwtSecurityConfig(tokenProvider, redisTemplate));

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        if ("prod".equals(Objects.requireNonNull(env.getProperty("spring.config.activate.on-profile")))) {
            config.setAllowedOrigins(
                    Arrays.asList(

                    )
            );
        } else {
            config.addAllowedOriginPattern("*");
        }

        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}