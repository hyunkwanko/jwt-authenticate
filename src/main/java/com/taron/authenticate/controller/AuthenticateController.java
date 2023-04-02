package com.taron.authenticate.controller;

import com.taron.authenticate.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authenticate Controller", description = "로그인 API")
public class AuthenticateController {
    private final MemberService memberService;

    @Operation(summary = "로그인", description = "로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return new ResponseEntity<>(memberService.login(loginRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "로그아웃", description = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(LogoutRequestDTO logoutRequestDTO) {
        return new ResponseEntity<>(memberService.logout(logoutRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "회원탈퇴", description = "회원탈퇴")
    @PostMapping("/withdrawal")
    public ResponseEntity<Boolean> withdrawal(WithdrawalRequestDTO withdrawalRequestDTO) {
        return new ResponseEntity<>(memberService.withdrawal(withdrawalRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "이메일 중복 체크", description = "이메일 중복 체크")
    @PostMapping("/checkDuplicateEmail")
    public ResponseEntity<Boolean> checkDuplicateEmail(@RequestBody DuplicateEmailRequestDTO duplicateEmailRequestDTO) {
        return new ResponseEntity<>(memberService.checkDuplicateEmail(duplicateEmailRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "닉네임 중복 체크", description = "닉네임 중복 체크")
    @PostMapping("/checkDuplicateNickname")
    public ResponseEntity<Boolean> checkDuplicateNickname(@RequestBody DuplicateNicknameRequestDTO duplicateNicknameRequestDTO) {
        return new ResponseEntity<>(memberService.checkDuplicateNickname(duplicateNicknameRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "비밀번호 찾기 - 메일 전송", description = "비밀번호 찾기 - 메일 전송")
    @PostMapping("/sendEmailFindPassword")
    public ResponseEntity<CertificationResponseDTO> sendEmailFindPassword(@RequestBody EmailFindPasswordRequestDTO emailFindPasswordRequestDTO) {
        return new ResponseEntity<>(memberService.sendEmailFindPassword(emailFindPasswordRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호 재설정")
    @PostMapping("/updatePassword")
    public ResponseEntity<UpdatePasswordResponseDTO> updatePassword(@RequestBody UpdatePasswordRequestDTO updatePasswordRequestDTO) {
        return new ResponseEntity<>(memberService.updatePassword(updatePasswordRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "토큰 재발급", description = "토큰 재발급")
    @PostMapping("/getAccessToken")
    public ResponseEntity<RefreshTokenResponseDTO> getAccessToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return new ResponseEntity<>(memberService.getAccessToken(refreshTokenRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "유저 정보", description = "유저 정보")
    @PreAuthorize("hasAnyRole('BASIC')")
    @PostMapping("/getUserInfo")
    public ResponseEntity<TokenResponseDTO> getUserInfo(@RequestBody TokenRequestDTO tokenRequestDTO) {
        return new ResponseEntity<>(memberService.getUserInfo(tokenRequestDTO), HttpStatus.OK);
    }
}
