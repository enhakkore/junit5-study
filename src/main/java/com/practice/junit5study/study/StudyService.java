package com.practice.junit5study.study;

import com.practice.junit5study.domain.Member;
import com.practice.junit5study.domain.Study;
import com.practice.junit5study.member.MemberService;

import java.util.Optional;

public class StudyService {

    private final MemberService memberService;
    private final StudyRepository studyRepository;

    public StudyService(MemberService memberService, StudyRepository studyRepository){
        assert memberService != null;
        assert studyRepository != null;
        this.memberService = memberService;
        this.studyRepository = studyRepository;
    }

    public Study createNewStudy(Long memberId, Study study){
        Optional<Member> member = memberService.findById(memberId);

        if(member.isPresent())
            study.setOwnerId(member.get().getId());
        else
            throw new IllegalArgumentException("Member doesn't exist for id: "+memberId);

        var newStudy = studyRepository.save(study);
        memberService.notify(newStudy);
        memberService.notify(member.get());

        return newStudy;
    }

    public Study openStudy(Study study) {
        study.open();
        Study openedStudy = studyRepository.save(study);
        memberService.notify(openedStudy);
        return openedStudy;
    }
}
