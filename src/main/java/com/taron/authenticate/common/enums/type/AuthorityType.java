package com.taron.authenticate.common.enums.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthorityType {
    MASTER("마스터"),
    BASIC("기본 권한");

    private final String description;

    public static AuthorityType findByType(String type) {
        try {
            return AuthorityType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
