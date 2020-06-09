package com.practice.junit5study.member;

import com.practice.junit5study.domain.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Test
    void createMemberService(@Mock MemberService memberService) {
        var expectedMember = new Member(1L, "user@gmail.com");
        Mockito.when(memberService.findById(1L)).thenReturn(Optional.of(expectedMember));
        assertEquals(Optional.of(expectedMember), memberService.findById(1L));
    }

}
