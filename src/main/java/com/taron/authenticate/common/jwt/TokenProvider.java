package com.taron.authenticate.common.jwt;

import com.taron.authenticate.common.enums.type.ErrorCodeType;
import com.taron.authenticate.common.util.CommonUtil;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenProvider {
    private final JwtDTO jwtDTO = JwtDTO.getInstance();
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public LoginResponseDTO generateToken(UserAndAuthorityRequestDTO userAndAuthorityRequestDTO) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userAndAuthorityRequestDTO.getUserId());
        claims.put("authority", userAndAuthorityRequestDTO.getAuthorityType());

        if (userAndAuthorityRequestDTO.getReIssueAccessToken()) {
            return createAccessToken(claims, userAndAuthorityRequestDTO);
        }

        return createAccessAndRefreshToken(claims, userAndAuthorityRequestDTO);
    }

    public LoginResponseDTO createAccessAndRefreshToken(Map<String, Object> claims, UserAndAuthorityRequestDTO userAndAuthorityRequestDTO) {
        Date createdDate = new Date();
        long accessTokenValidTime = Long.parseLong(Objects.requireNonNull(jwtDTO.getAccessTokenValidTime()));
        long refreshTokenValidTime = Long.parseLong(Objects.requireNonNull(jwtDTO.getRefreshTokenValidTime()));

        // User Type + User Id
        String redisKey = userAndAuthorityRequestDTO.getAuthorityType() + "_" + userAndAuthorityRequestDTO.getUserId().toString();

        // Access Token
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(redisKey)
                .setIssuedAt(createdDate)
                .setExpiration(new Date(createdDate.getTime() + accessTokenValidTime * 1000))
                .signWith(jwtDTO.getAccessSecretKey())
                .compact();

        // Refresh Token
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(redisKey)
                .setIssuedAt(createdDate)
                .setExpiration(new Date(createdDate.getTime() + refreshTokenValidTime * 1000))
                .signWith(jwtDTO.getRefreshSecretKey())
                .compact();

        return LoginResponseDTO.builder()
                .userId(userAndAuthorityRequestDTO.getUserId())
                .accessToken(accessToken)
                .accessTokenExpiredIn(String.valueOf(accessTokenValidTime))
                .refreshToken(refreshToken)
                .refreshTokenExpiredIn(String.valueOf(refreshTokenValidTime))
                .build();
    }

    public LoginResponseDTO createAccessToken(Map<String, Object> claims, UserAndAuthorityRequestDTO userAndAuthorityRequestDTO) {
        Date createdDate = new Date();
        long accessTokenValidTime = Long.parseLong(Objects.requireNonNull(jwtDTO.getAccessTokenValidTime()));

        // User Type + User Id
        String redisKey = userAndAuthorityRequestDTO.getAuthorityType() + "_" + userAndAuthorityRequestDTO.getUserId().toString();

        // Access Token
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setSubject(redisKey)
                .setIssuedAt(createdDate)
                .setExpiration(new Date(createdDate.getTime() + accessTokenValidTime * 1000))
                .signWith(jwtDTO.getAccessSecretKey())
                .compact();

        return LoginResponseDTO.builder()
                .userId(userAndAuthorityRequestDTO.getUserId())
                .accessToken(accessToken)
                .accessTokenExpiredIn(String.valueOf(accessTokenValidTime))
                .build();
    }

    public void validateToken(String token, Key secretKey) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new ResponseStatusException(HttpStatus.OK, ErrorCodeType.MALFORMED_TOKEN.name());
        } catch (ExpiredJwtException e) {
            throw new ResponseStatusException(HttpStatus.OK, ErrorCodeType.EXPIRED_TOKEN.name());
        } catch (UnsupportedJwtException e) {
            throw new ResponseStatusException(HttpStatus.OK, ErrorCodeType.UNSUPPORTED_TOKEN.name());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.OK, ErrorCodeType.ILLEGAL_TOKEN.name());
        }
    }

    public void validateTokenByFilter(String token, Key secretKey) {
        Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(CommonUtil.TOKEN_PREFIX)) {
            return bearerToken.substring(7);
        }

        return null;
    }

    public Claims extractAllClaims(String token, Key secretKey) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    public Authentication getAuthentication(String token, Key secretKey) {
        Claims claims = extractAllClaims(token, secretKey);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("authority").toString().split(","))
                        .map(authority -> CommonUtil.ROLE_PREFIX + authority)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public String extractUsername(String token, Key secretKey) {
        return extractAllClaims(token, secretKey).getSubject();
    }

    public Date extractExpiration(String token, Key secretKey) {
        return extractClaim(token, Claims::getExpiration, secretKey);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, Key secretKey) {
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }
}
