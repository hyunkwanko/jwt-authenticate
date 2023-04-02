package com.taron.authenticate.repository;

import com.taron.authenticate.domain.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    Optional<MemberEntity> findOneByEmail(String email);
    Optional<MemberEntity> findOneById(Long id);
    Optional<MemberEntity> findOneByNickname(String nickname);
}
