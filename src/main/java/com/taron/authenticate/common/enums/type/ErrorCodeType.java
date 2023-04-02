package com.taron.authenticate.common.enums.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCodeType {

    NOT_EXIST_TOKEN("901", "JWT 토큰이 존재하지 않습니다."),
    MALFORMED_TOKEN("902", "잘못된 JWT 서명입니다."),
    EXPIRED_TOKEN("903", "만료된 JWT 토큰입니다. 재로그인 해주시길 바랍니다."),
    UNSUPPORTED_TOKEN("904", "지원되지 않는 JWT 토큰입니다."),
    ILLEGAL_TOKEN("905", "JWT 토큰이 잘못되었습니다.");

    private String errorCode;
    private String errorMessage;
}
