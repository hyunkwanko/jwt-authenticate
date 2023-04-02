package com.taron.authenticate.common.enums.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberStatus {
	ACTIVE("계정 활성"),
	LOGIN_FAIL("로그인 실패"),
	INACTIVE("휴면 상태"),
	WITHDRAWAL("탈퇴"),
	BAN("영구 차단"),
	BLOCK("일시적 차단");

	private final String description;

	public static MemberStatus findByType(String type) {
		try {
			return MemberStatus.valueOf(type);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
