package com.taron.authenticate.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taron.authenticate.common.enums.type.AuthorityType;
import com.taron.authenticate.common.enums.type.ErrorCodeType;
import com.taron.authenticate.common.util.CommonUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    private final TokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtDTO jwtDTO = JwtDTO.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

        String jwt = tokenProvider.resolveToken(httpServletRequest);

        log.info(httpServletRequest.getRequestURL().toString());

        try {
            // 토큰 유무 확인, 에러 페이지가 아닐 경우
            if (StringUtils.hasText(jwt) && !httpServletRequest.getRequestURL().toString().contains("error")) {
                // 토큰 유효 확인
                tokenProvider.validateTokenByFilter(jwt, jwtDTO.getAccessSecretKey());

                // 인증 객체 생성
                Authentication authentication = tokenProvider.getAuthentication(jwt, jwtDTO.getAccessSecretKey());

                // 레디스에서 Refresh 토큰 확인
                ValueOperations<String, String> refreshToken = redisTemplate.opsForValue();
                if (refreshToken.get(authentication.getName()) == null) {
                    objectMapper.writeValue(httpServletResponse.getWriter(), CommonUtil.successResponse(ErrorCodeType.LOGIN_ERROR));
                    return;
                }

                SecurityContextHolder.getContext().setAuthentication(authentication);
                getUserInfo(authentication);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            objectMapper.writeValue(httpServletResponse.getWriter(), CommonUtil.successResponse((ErrorCodeType.MALFORMED_TOKEN)));
        } catch (ExpiredJwtException e) {
            objectMapper.writeValue(httpServletResponse.getWriter(), CommonUtil.successResponse((ErrorCodeType.EXPIRED_TOKEN)));
        } catch (UnsupportedJwtException e) {
            objectMapper.writeValue(httpServletResponse.getWriter(), CommonUtil.successResponse((ErrorCodeType.UNSUPPORTED_TOKEN)));
        } catch (IllegalArgumentException e) {
            objectMapper.writeValue(httpServletResponse.getWriter(), CommonUtil.successResponse((ErrorCodeType.ILLEGAL_TOKEN)));
        }
    }

    public void getUserInfo(Authentication authentication) {
        String[] authorityAndUserId = authentication.getName().split("_");
        UserBaseInfoDTO userBaseInfoDTO = UserBaseInfoDTO.getInstance();
        userBaseInfoDTO.setUserId(Long.parseLong(authorityAndUserId[1]));
        userBaseInfoDTO.setAuthorities(
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .map(x -> AuthorityType.findByType(x.replace(CommonUtil.ROLE_PREFIX, "")))
                        .collect(Collectors.toList())
        );

        log.info("Auth Info -> userId: {} / authorities: {}",
                userBaseInfoDTO.getUserId(),
                userBaseInfoDTO.getAuthorities()
        );
    }
}
