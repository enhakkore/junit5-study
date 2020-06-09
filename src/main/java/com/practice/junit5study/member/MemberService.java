package com.practice.junit5study.member;

import com.practice.junit5study.domain.Member;
import com.practice.junit5study.domain.Study;

import java.util.Optional;

public interface MemberService{
    Optional<Member> findById(Long memberId);
    void validate(Long memberId);

    void notify(Study newStudy);

    void notify(Member member);
}
