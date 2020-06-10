package com.practice.junit5study.web;

import com.practice.junit5study.domain.Member;
import com.practice.junit5study.member.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class BasicController {

    private final MemberService memberService;

    public BasicController(MemberService memberService){
        this.memberService = memberService;
    }

    @GetMapping("/get")
    public Member getMemberById(Long memberId){
        Optional<Member> member = memberService.findById(memberId);
        return member.orElseThrow(() -> new IllegalArgumentException("해당 id에 대한 Member가 존재하지 않습니다. id : " + memberId));
    }
}
