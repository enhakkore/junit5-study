package com.practice.junit5study.study;

import com.practice.junit5study.domain.Member;
import com.practice.junit5study.domain.Study;
import com.practice.junit5study.member.MemberService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/*
* Mockito가 하는 일
* 1.
*   StudyService를 만들기 위해서는 memberService와 StudyRepository가 필요하다.
*   하지만 프로젝트에는 두 개다 interface만 있고 구현체가 없다.
*   테스트를 위해 직접 임의로 interface 구현체를 만들어 StudyService를 만들 수 있지만 Mockito를 사용하면 Mockito가 이를 대신해준다.
*   아래 방법 1, 2, 3 참고
*   @Mock 을 사용하기 위해서는 @ExtendWith(MockitoExtension.class)가 필요하다.
*
* 2.
*   Mock.when()을 사용해 특정 메서드가 호출되면 일어나는 일을 가정할 수 있다.
*
* */

@ExtendWith(MockitoExtension.class)
public class StudyServiceTest {

    // 방법2 -> static 필드에 정의하기
//    @Mock MemberService memberService;
//    @Mock StudyRepository studyRepository;

    @Test                   // 방법3 -> 파라미터로 넘기기
    void createStudyService(@Mock MemberService memberService, @Mock StudyRepository studyRepository) {
        // 방법1
//        MemberService memberService = Mockito.mock(MemberService.class);
//        StudyRepository studyRepository = Mockito.mock(StudyRepository.class);

        StudyService studyService = new StudyService(memberService, studyRepository);
        assertNotNull(studyService);

        Member member = new Member(1L, "user@gmail.com");

        // 어떤 메서드가 호출됐을 때 Exception을 throw하도록 가정하고, 테스트하기
        Mockito.doThrow(new IllegalArgumentException()).when(memberService).validate(1L);
        assertThrows(IllegalArgumentException.class, () -> memberService.validate(1L));

        // memberService.findById 가 호출됐을 때 가정하기
        //      findById 메서드의 파라미터로 ArgumentMatchers.anyLong()를 주었기때문에
        //      어떤 Long 타입 값을 넣던지 아래에서 가정한대로 흘러간다.
        Mockito.when(memberService.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(member))    // 첫 번쩨 호출됐을 때
                .thenThrow(new RuntimeException())  // 두 번째 호출됐을 때
                .thenReturn(Optional.empty());      // 세 번째 호출됐을 때

        // 첫 번째 호출
        Optional<Member> expectedMember = memberService.findById(1L);
        assertTrue(expectedMember.isPresent());
        assertEquals("user@gmail.com", expectedMember.get().getEmail());

        // 두 번째 호출
        assertThrows(RuntimeException.class, () -> memberService.findById(2L));

        // 세 번째 호출
        assertEquals(Optional.empty(), memberService.findById(3L));
    }

    @Test
    void createNewStudyTest(@Mock MemberService memberService, @Mock StudyRepository studyRepository){
        StudyService studyService = new StudyService(memberService, studyRepository);

        Member member = new Member(1L, "user@gmail.com");
        Study study = new Study(10L, "test");

        Mockito.when(memberService.findById(1L)).thenReturn(Optional.of(member));
        Mockito.when(studyRepository.save(study)).thenReturn(study);

        studyService.createNewStudy(member.getId(), study);
        assertEquals(member.getId(), study.getOwnerId());

        // Mockito.verify() -> 메서드가 몇 번 호출됐는지 확인
        Mockito.verify(memberService, Mockito.times(1)).notify(study);

        // memberService.notify(study) 이후에 더 이상 memberService를 호출하지 않는지 확인
//        Mockito.verifyNoMoreInteractions(memberService);
        // 하지만 verifyNoMoreInteractions()은 실패하는데, memberService.notify(member)가 호출되기 때문이다.

        Mockito.verify(memberService, Mockito.times(1)).notify(member);
        Mockito.verify(memberService, Mockito.never()).validate(member.getId());

        // 아래에 가정한 순서대로 메서드가 호출됐는지 확인
        InOrder inOrder = Mockito.inOrder(memberService);
        inOrder.verify(memberService).notify(study);
        inOrder.verify(memberService).notify(member);
    }

    @Test
    void createNewStudyTest_BDD(@Mock MemberService memberService, @Mock StudyRepository studyRepository){

        // Given
        var studyService = new StudyService(memberService, studyRepository);

        Member member = new Member(1L, "user@gmail.com");
        Study study = new Study(10L, "test");

        BDDMockito.given(memberService.findById(member.getId())).willReturn(Optional.of(member));
        BDDMockito.given(studyRepository.save(study)).willReturn(study);


        // When
        studyService.createNewStudy(member.getId(), study);


        // Then
        assertEquals(member.getId(), study.getOwnerId());
        BDDMockito.then(memberService).should(Mockito.times(1)).notify(study);
        BDDMockito.then(memberService).should(Mockito.times(1)).notify(member);
        BDDMockito.then(memberService).shouldHaveNoMoreInteractions();

    }

    @Test
    void openStudyTest(@Mock MemberService memberService, @Mock StudyRepository studyRepository){

        // Given
        StudyService studyService = new StudyService(memberService, studyRepository);
        Study study = new Study(10L, "더 자바, 테스트");
        BDDMockito.given(studyRepository.save(study)).willReturn(study);

        // When
        studyService.openStudy(study);

        // Then
        assertEquals(StudyStatus.OPENED, study.getStatus());
        assertNotNull(study.getOpenedDateTime());
        BDDMockito.then(memberService).should().notify(study);
    }
}
