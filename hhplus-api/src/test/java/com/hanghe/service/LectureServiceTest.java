package com.hanghe.service;

import com.hanghe.domain.lecture.entity.Lecture;
import com.hanghe.domain.lecture.repository.LectureRepository;
import com.hanghe.domain.lecture.service.LectureService;
import com.hanghe.domain.lectureRegistration.repository.LectureRegistrationRepository;
import com.hanghe.domain.lectureRegistration.state.LectureRegistrationState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;

public class LectureServiceTest {

    @InjectMocks
    private LectureService sut;
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private LectureRegistrationRepository lectureRegistrationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("수강 가능한 강의 체크")
    void find_possible_lecture_test(){
        // given
        LocalDate now = LocalDate.now();
        Lecture lecture1 = Lecture.builder()
                .lIdx(1L)
                .lTitle("TDD")
                .lDescription("Learn TDD")
                .lDate(now)
                .lLimit(30)
                .build();

        Lecture lecture2 = Lecture.builder()
                .lIdx(2L)
                .lTitle("Clean Architecture")
                .lDescription("Learn Clean Architecture")
                .lDate(now)
                .lLimit(30)
                .build();

        Lecture lecture3 = Lecture.builder()
                .lIdx(3L)
                .lTitle("Java")
                .lDescription("Learn Java")
                .lDate(now)
                .lLimit(30)
                .build();

        // when
        when(lectureRepository.findAll()).thenReturn(Arrays.asList(lecture1, lecture2, lecture3));

        // 각 강의별 수강 인원 세팅
        when(lectureRegistrationRepository.countByLectureAndLrState(lecture1,LectureRegistrationState.APPLY)).thenReturn(20); // 신청 가능
        when(lectureRegistrationRepository.countByLectureAndLrState(lecture2,LectureRegistrationState.APPLY)).thenReturn(30); // 신청 불가
        when(lectureRegistrationRepository.countByLectureAndLrState(lecture3,LectureRegistrationState.APPLY)).thenReturn(10); // 신청 가능

        List<Lecture> possibleLectures = sut.findPossibleLectureList();

        // then
        Assertions.assertEquals(2, possibleLectures.size());
        Assertions.assertEquals(1L, possibleLectures.get(0).getLIdx());
        Assertions.assertEquals(3L, possibleLectures.get(1).getLIdx());

    }

}
