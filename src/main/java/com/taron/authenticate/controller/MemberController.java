package com.taron.authenticate.controller;

import com.taron.authenticate.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/member")
@Tag(name = "Member Controller", description = "회원가입 API")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "회원가입", method = "POST")
    @PostMapping("/signUp")
    public ResponseEntity<MemberResponseDTO> signUp(@Valid @RequestBody MemberRequestDTO memberRequestDTO) {
        return new ResponseEntity<>(memberService.signUp(memberRequestDTO), HttpStatus.OK);
    }

    @Operation(summary = "회원가입 - 메일 전송", description = "회원가입 - 메일 전송")
    @PostMapping("/sendEmailMemberSignUp")
    public ResponseEntity<CertificationResponseDTO> sendEmailMemberSignUp(@RequestBody EmailMemberSignUpRequestDTO emailMemberSignUpRequestDTO) {
        return new ResponseEntity<>(memberService.sendEmailMemberSignUp(emailMemberSignUpRequestDTO), HttpStatus.OK);
    }
}
