package com.hanghe.service;

import com.hanghe.domain.lecture.entity.Lecture;
import com.hanghe.domain.lecture.repository.LectureRepository;
import com.hanghe.domain.lectureRegistration.entity.LectureRegistration;
import com.hanghe.domain.lectureRegistration.repository.LectureRegistrationRepository;
import com.hanghe.domain.lectureRegistration.service.LectureRegistrationService;
import com.hanghe.domain.lectureRegistration.state.LectureRegistrationState;
import com.hanghe.domain.user.entity.User;
import com.hanghe.domain.user.repository.UserRepository;
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
import java.util.Optional;

import static org.mockito.Mockito.*;

public class LectureRegistrationServiceTest {

    @InjectMocks
    private LectureRegistrationService sut;

    @Mock
    private LectureRegistrationRepository lectureRegistrationRepository;
    @Mock
    private LectureRepository lectureRepository;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("강의 신청 테스트")
    void lecture_registration_test(){
        // given
        Lecture lecture = new Lecture(1L,"TDD","Learn TDD","강사",LocalDate.now(),30);
        User user = new User(1L,"testId","테스트");
        LectureRegistration lectureRegistration = LectureRegistration.builder()
                .lrIdx(1L)
                .lecture(lecture)
                .user(user)
                .lrState(LectureRegistrationState.APPLY)
                .build();

        // Mock save
        when(lectureRegistrationRepository.save(lectureRegistration))
                .thenReturn(lectureRegistration);
        when(lectureRepository.findWithLockByLIdx(lecture.getLIdx())).thenReturn(Optional.of(lecture));

        // when
        LectureRegistration result = sut.registerLecture(lectureRegistration);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(lecture, result.getLecture());
        Assertions.assertEquals(user, result.getUser());
        Assertions.assertEquals(LectureRegistrationState.APPLY, result.getLrState());

    }

    @Test
    @DisplayName("취소 유저 강의 재신청 테스트")
    void lecture_registration_cancelUser_test(){
        // given
        Lecture lecture = new Lecture(1L,"TDD","Learn TDD","강사",LocalDate.now(),30);
        User user = new User(1L,"testId","테스트");
        LectureRegistration canceledRegistration = LectureRegistration.builder()
                .lrIdx(1L)
                .lecture(lecture)
                .user(user)
                .lrState(LectureRegistrationState.CANCEL)
                .build();

        // Mock save
        when(lectureRegistrationRepository.findByUserAndLectureAndLrState(user,lecture,LectureRegistrationState.CANCEL))
                .thenReturn(canceledRegistration);
        when(lectureRepository.findWithLockByLIdx(lecture.getLIdx())).thenReturn(Optional.of(lecture));

        // when
        LectureRegistration result = sut.registerLecture(canceledRegistration);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(canceledRegistration.getLrIdx(), result.getLrIdx());
        Assertions.assertEquals(LectureRegistrationState.APPLY, result.getLrState());
    }

    @Test
    @DisplayName("이미 신청한 유저 테스트")
    void lecture_registration_existingUser_test() {
        // given
        Lecture lecture = new Lecture(1L,"TDD","Learn TDD","강사",LocalDate.now(),30);
        User user = new User(1L,"testId","테스트");
        LectureRegistration existingRegistration = LectureRegistration.builder()
                .lrIdx(1L)
                .lecture(lecture)
                .user(user)
                .lrState(LectureRegistrationState.APPLY)
                .build();
        // when
        when(lectureRegistrationRepository.findByUserAndLectureAndLrState(user,lecture,LectureRegistrationState.APPLY))
                .thenReturn(existingRegistration);

        // then
        Assertions.assertThrows(RuntimeException.class, () -> {
            sut.registerLecture(existingRegistration);
        }, "이미 신청완료된 유저입니다.");
    }

    @Test
    @DisplayName("인원 마감된 강의 신청 테스트")
    void lecture_registration_fullUser_test() {
        // given
        Lecture lecture = new Lecture(1L,"TDD","Learn TDD","강사",LocalDate.now(),30);
        User user = new User(1L,"testId","테스트");
        LectureRegistration registration = LectureRegistration.builder()
                .lrIdx(1L)
                .lecture(lecture)
                .user(user)
                .lrState(LectureRegistrationState.APPLY)
                .build();

        // when
        when(lectureRegistrationRepository.countByLectureAndLrState(lecture, LectureRegistrationState.APPLY))
                .thenReturn(30); // 이미 꽉 찬 상태

        // then
        Assertions.assertThrows(RuntimeException.class, () -> {
            sut.registerLecture(registration);
        }, "수강 신청 인원이 가득찼습니다.");
    }

    @Test
    @DisplayName("유저의 신청완료된 특강 목록")
    void find_user_allLectures(){
        // given
        Lecture lecture1 = new Lecture(1L,"TDD","Learn TDD","강사",LocalDate.now(),30);
        Lecture lecture2 = new Lecture(2L,"Clean Architecture","Learn Clean Architecture","강사2",LocalDate.now(),30);
        User user = new User(1L,"testId","테스트");
        LectureRegistration registration1 = LectureRegistration.builder()
                .lrIdx(1L)
                .lecture(lecture1)
                .user(user)
                .lrState(LectureRegistrationState.APPLY)
                .build();

        LectureRegistration registration2 = LectureRegistration.builder()
                .lrIdx(2L)
                .lecture(lecture2)
                .user(user)
                .lrState(LectureRegistrationState.APPLY)
                .build();

        // when
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(lectureRegistrationRepository.findAllByUserAndLrState(user,LectureRegistrationState.APPLY)).thenReturn(Arrays.asList(registration1, registration2));

        List<LectureRegistration> result = sut.findLectureRegistrationListByUser(user.getUIdx());

        // then
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(1L, result.get(0).getLrIdx());
        Assertions.assertEquals(2L, result.get(1).getLrIdx());
    }

}
