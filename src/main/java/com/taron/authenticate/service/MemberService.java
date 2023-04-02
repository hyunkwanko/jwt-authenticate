package com.taron.authenticate.service;


import com.taron.authenticate.common.enums.type.AuthorityType;
import com.taron.authenticate.domain.MemberEntity;

public interface MemberService {

    Optional<MemberEntity> getMemberEntityByEmail(String email);

    Optional<MemberEntity> getMemberEntityById(Long userId);

    MemberResponseDTO MemberSignUp(MemberRequestDTO MemberRequestDTO);

    void loginSuccess(Long userId, AuthorityType authorityType);

    void loginFail(Long userId, AuthorityType authorityType);

    LoginInfoResponseDTO getLoginInfo(LoginRequestDTO loginRequestDTO);

    Boolean withdrawal(WithdrawalRequestDTO withdrawalRequestDTO);

    Boolean isMemberByEmail(String email);

    Boolean isMemberByNickname(String nickname);

    UpdatePasswordResponseDTO updatePassword(UpdatePasswordRequestDTO updatePasswordRequestDTO);
}