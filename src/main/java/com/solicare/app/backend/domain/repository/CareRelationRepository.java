package com.solicare.app.backend.domain.repository;

import com.solicare.app.backend.domain.entity.Care;
import com.solicare.app.backend.domain.entity.Member;
import com.solicare.app.backend.domain.entity.Senior;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareRelationRepository extends JpaRepository<Care, String> {
    List<Care> findByMemberOrderBySenior_NameAsc(Member member);

    List<Care> findBySeniorOrderByMember_NameAsc(Senior senior);

    boolean existsByMemberAndSenior(Member member, Senior senior);
}
