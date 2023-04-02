package com.taron.authenticate.common.enums.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountType {
    EMAIL("EMAIL 로그인"),
    KAKAO("KAKAO 로그인");

    private final String description;

    public static AccountType findByType(String type) {
        try {
            return AccountType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
