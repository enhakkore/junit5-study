package com.practice.junit5study.study;

import com.practice.junit5study.domain.Study;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class StudyRepositoryTest {

    @Test
    void createStudyRepositoryTest(@Mock StudyRepository studyRepository){
        var expectedStudy = new Study(10L, "java");
        Mockito.when(studyRepository.save(expectedStudy)).thenReturn(expectedStudy);
        assertEquals(expectedStudy, studyRepository.save(expectedStudy));
    }
}
