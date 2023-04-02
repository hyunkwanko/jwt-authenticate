package com.taron.authenticate.domain;

import com.taron.authenticate.common.enums.status.MemberStatus;
import com.taron.authenticate.domain.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "member")
@Entity
public class MemberEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("ID")
    @Column(nullable = false)
    private Long id;

    @Comment("이메일")
    @Column(name = "email", length = 50, unique = true)
    private String email;

    @Comment("닉네임")
    @Column(name = "nickname", length = 50)
    private String nickname;

    @Comment("비밀번호")
    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Comment("실패 횟수")
    @Column(name = "fail_count")
    private int failCount;

    @Comment("상태")
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Comment("마지막 로그인 일시")
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Comment("마지막 로그인 실패 일시")
    @Column(name = "last_login_fail_at")
    private LocalDateTime lastLoginFailAt;

    @Comment("비밀번호 수정일시")
    @Column(name = "password_updated_at")
    private LocalDateTime passwordUpdatedAt;
}