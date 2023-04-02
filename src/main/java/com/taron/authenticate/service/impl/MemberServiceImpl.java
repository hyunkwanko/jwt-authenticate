package com.taron.authenticate.service.impl;

import com.taron.authenticate.common.enums.status.MemberStatus;
import com.taron.authenticate.common.enums.type.AccountType;
import com.taron.authenticate.common.enums.type.AuthorityType;
import com.taron.authenticate.common.enums.type.ErrorCodeType;
import com.taron.authenticate.domain.MemberEntity;
import com.taron.authenticate.repository.MemberRepository;
import com.taron.authenticate.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    public static final int FAIL_COUNT_MAX = 10;

    @Transactional
    @Override
    public MemberResponseDTO memberSignUp(memberRequestDTO memberRequestDTO) {
        if (isMemberByEmail(memberRequestDTO.getEmail())) {
            throw new ResponseStatusException(HttpStatus.OK, ErrorCodeType.EXISTS_MEMBER.name());
        }

        MemberEntity memberEntity = memberRepository.save(
                MemberEntity.builder()
                        .email(memberRequestDTO.getEmail())
                        .nickname(memberRequestDTO.getNickname())
                        .password(passwordEncoder.encode(memberRequestDTO.getPassword()))
                        .status(MemberStatus.ACTIVE)
                        .passwordUpdatedAt(LocalDateTime.now())
                        .build()
        );

        return memberResponseDTO.builder()
                .email(memberRequestDTO.getEmail())
                .nickname(memberRequestDTO.getNickname())
                .accountType(AccountType.EMAIL)
                .build();
    }

    @Transactional
    @Override
    public void loginSuccess(Long userId, AuthorityType authorityType) {
        memberLoginSuccess(userId);
    }

    @Transactional
    @Override
    public void loginFail(Long userId, AuthorityType authorityType) {
        memberLoginFail(userId);
    }

    @Transactional
    @Override
    public LoginInfoResponseDTO getLoginInfo(LoginRequestDTO loginRequestDTO) {
        LocalDateTime localDateTime = LocalDateTime.now();

        return getMemberLoginInfo(loginRequestDTO, localDateTime);
    }

    @Transactional
    @Override
    public Boolean withdrawal(WithdrawalRequestDTO withdrawalRequestDTO) {
        Long userId = withdrawalRequestDTO.getUserInfo().getUserId();
        List<AuthorityType> authorities = withdrawalRequestDTO.getUserInfo().getAuthorities();

        MemberEntity memberEntity = getMemberEntityById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.OK, ErrorCodeType.NOT_FOUND_MEMBER.name()));

        memberEntity.setStatus(MemberStatus.WITHDRAWAL);

        return true;
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean isMemberByEmail(String email) {
        return memberRepository.findOneByEmail(email).isPresent();
    }

    @Transactional(readOnly = true)
    @Override
    public Boolean isMemberByNickname(String nickname) {
        return memberRepository.findOneByNickname(nickname).isPresent();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<MemberEntity> getMemberEntityByEmail(String email) {
        return memberRepository.findOneByEmail(email);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<MemberEntity> getMemberEntityById(Long userId) {
        return memberRepository.findOneById(userId);
    }

    @Transactional
    @Override
    public UpdatePasswordResponseDTO updatePassword(UpdatePasswordRequestDTO updatePasswordRequestDTO) {
        Optional<MemberEntity> member = getMemberEntityByEmail(updatePasswordRequestDTO.getEmail());
        if (member.isPresent()) {
            member.get().setPassword(passwordEncoder.encode(updatePasswordRequestDTO.getPassword()));
            return UpdatePasswordResponseDTO.builder()
                    .email(member.get().getEmail())
                    .build();
        }

        throw new ResponseStatusException(HttpStatus.OK, ErrorCodeType.NOT_FOUND_MEMBER.name());
    }

    private LoginInfoResponseDTO getMemberLoginInfo(LoginRequestDTO loginRequestDTO, LocalDateTime localDateTime) {
        Boolean isBlocked = null;
        MemberEntity memberEntity = memberRepository.findOneByEmail(loginRequestDTO.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.OK, ErrorCodeType.NOT_FOUND_MEMBER.name()));

        // BLOCK 경우
        if (memberEntity.getStatus().equals(MemberStatus.BLOCK)) {
            // 마지막 로그인 실패 일시로부터 5분이 지난 경우
            if (localDateTime.isAfter(memberEntity.getLastLoginFailAt().plusMinutes(5))) {
                memberEntity.setFailCount(0);
                memberEntity.setStatus(MemberStatus.ACTIVE);
                isBlocked = false;
            } else {
                isBlocked = true;
            }
        } else {
            isBlocked = false;
        }

        return LoginInfoResponseDTO.builder()
                .userId(memberEntity.getId())
                .status(memberEntity.getStatus())
                .isBlocked(isBlocked)
                .failCount(memberEntity.getFailCount())
                .lastLoginFailAt(memberEntity.getLastLoginFailAt())
                .passwordUpdatedAt(memberEntity.getPasswordUpdatedAt())
                .build();
    }

    private void memberLoginSuccess(Long userId) {
        MemberEntity memberEntity = memberRepository.findOneById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.OK, ErrorCodeType.NOT_FOUND_MEMBER.name()));

        LocalDateTime localDateTime = LocalDateTime.now();

        // 마지막 로그인 일시
        memberEntity.setLastLoginAt(localDateTime);

        // 마지막 로그인 실패 일시
        memberEntity.setLastLoginFailAt(null);

        // 로그인 실패 횟수
        memberEntity.setFailCount(0);

        // 계정 활성화
        memberEntity.setStatus(MemberStatus.ACTIVE);
        log.info("계정 ID: {} / 계정명: {} / 권한: {} / 계정 상태: {} / 마지막 로그인 일시: {}",
                memberEntity.getId(),
                memberEntity.getEmail(),
                AuthorityType.BASIC,
                memberEntity.getStatus(),
                memberEntity.getLastLoginAt()
        );
    }

    private void memberLoginFail(Long userId) {
        MemberEntity memberEntity = memberRepository.findOneById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.OK, ErrorCodeType.NOT_FOUND_MEMBER.name()));

        LocalDateTime localDateTime = LocalDateTime.now();

        // 로그인 실패 횟수가 10회 이상
        if (memberEntity.getFailCount() >= FAIL_COUNT_MAX) {
            // 유저 상태
            memberEntity.setStatus(MemberStatus.BLOCK);
        } else {
            // 로그인 실패 횟수 증가
            memberEntity.setFailCount(memberEntity.getFailCount() + 1);

            // 유저 상태
            memberEntity.setStatus(MemberStatus.LOGIN_FAIL);

            // 마지막 로그인 실패 일시
            memberEntity.setLastLoginFailAt(localDateTime);
        }

        log.info("계정명: {} / 계정 상태: {} / 권한: {} / 로그인 실패 횟수: {} / 마지막 로그인 실패 일시: {}",
                memberEntity.getEmail(),
                memberEntity.getStatus(),
                AuthorityType.BASIC,
                memberEntity.getFailCount(),
                memberEntity.getLastLoginFailAt()
        );
    }
}
