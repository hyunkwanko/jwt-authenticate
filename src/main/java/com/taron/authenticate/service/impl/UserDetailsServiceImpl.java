package com.taron.authenticate.service.impl;

import com.taron.authenticate.common.enums.type.AuthorityType;
import com.taron.authenticate.common.enums.type.ErrorCodeType;
import com.taron.authenticate.domain.MemberEntity;
import com.taron.authenticate.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserDetailsServiceImpl implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdAndAuthorityType) {
        try {
            // [0]: User Id, [1]: 계정 권한
            String[] getUserIdAndAuthorityType = userIdAndAuthorityType.split(",");

            // 권한 리스트
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

            return getUser(getUserIdAndAuthorityType, grantedAuthorities);
        } catch (UsernameNotFoundException usernameNotFoundException) {
            log.error(usernameNotFoundException.getMessage());
        }

        throw new ResponseStatusException(HttpStatus.OK, ErrorCodeType.NOT_FOUND_MEMBER.name());
    }

    private User getUser(String[] getUserIdAndAuthorityType, List<GrantedAuthority> grantedAuthorities) {
        MemberEntity memberEntity = memberRepository.findOneById(Long.parseLong(getUserIdAndAuthorityType[0]))
                .orElseThrow(() -> new UsernameNotFoundException(getUserIdAndAuthorityType[0] + " -> 데이터베이스에서 찾을 수 없습니다."));

        grantedAuthorities.add(new SimpleGrantedAuthority(String.valueOf(AuthorityType.BASIC)));

        return new User(
                memberEntity.getId().toString(),
                memberEntity.getPassword(),
                grantedAuthorities
        );
    }
}