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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class LectureRegistrationServiceConcurrencyTest {

    @Autowired
    private LectureRegistrationService lectureRegistrationService;

    @Autowired
    private LectureRepository lectureRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LectureRegistrationRepository lectureRegistrationRepository;

    private Lecture lecture;
    private List<User> users;

    private int userCount = 40;
    private int lectureLimit = 30;

    @BeforeEach
    @Transactional
    void setUp() {

        lecture = lectureRepository.save(
                Lecture.builder()
                        .lTitle("TDD")
                        .lDescription("Learn TDD")
                        .lLecturer("강사1")
                        .lDate(LocalDate.now())
                        .lLimit(lectureLimit)
                        .build()
        );

        users = new ArrayList<>();
        for (int i = 1; i <= userCount; i++) {
            User user = userRepository.save(
                    User.builder()
                            .uId("testId"+i)
                            .uName("테스트유저" + i)
                            .build()
            );
            users.add(user);
        }
    }

    @Test
    @DisplayName("같은 유저 여러번 수강 신청 테스트")
    void concurrent_LectureRegistration_sameUser_test() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<LectureRegistration>> futures = new ArrayList<>();
        User user = userRepository.save(
                User.builder()
                        .uId("testId")
                        .uName("테스트유저" )
                        .build()
        );

        for (int i=0;i<5;i++) {
            Future<LectureRegistration> future = executorService.submit(() -> {
                try {
                    return lectureRegistrationService.registerLecture(
                            LectureRegistration.builder()
                                    .user(user)
                                    .lecture(lecture)
                                    .lrState(LectureRegistrationState.APPLY)
                                    .build()
                    );
                } catch (RuntimeException e) {
                    return null;
                }
            });
            futures.add(future);
        }

        // 결과 확인
        int successCount = 0;
        for (Future<LectureRegistration> future : futures) {
            LectureRegistration registration = future.get();
            if (registration != null) {
                successCount++;
            }
        }

        // DB에서 성공적인 등록 수 확인
        long successfulRegistrations = lectureRegistrationRepository.countByLectureAndLrState(
                lecture,
                LectureRegistrationState.APPLY
        );

        // JUnit Assertions
        Assertions.assertEquals(1, successCount);
        Assertions.assertEquals(1, successfulRegistrations);

        executorService.shutdown();

    }

    @Test
    @DisplayName("30명 제한 강의에서 40명 동시에 신청 테스트")
    void concurrent_LectureRegistration_overLimit_test() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        List<Future<LectureRegistration>> futures = new ArrayList<>();

        for (User user : users) {
            Future<LectureRegistration> future = executorService.submit(() -> {
                try {
                    return lectureRegistrationService.registerLecture(
                            LectureRegistration.builder()
                                    .user(user)
                                    .lecture(lecture)
                                    .lrState(LectureRegistrationState.APPLY)
                                    .build()
                    );
                } catch (RuntimeException e) {
                    return null;
                }
            });
            futures.add(future);
        }

        // 결과 확인
        int successCount = 0;
        for (Future<LectureRegistration> future : futures) {
            LectureRegistration registration = future.get();
            if (registration != null) {
                successCount++;
            }
        }

        // DB에서 성공적인 등록 수 확인
        long successfulRegistrations = lectureRegistrationRepository.countByLectureAndLrState(
                lecture,
                LectureRegistrationState.APPLY
        );

        // JUnit Assertions
        Assertions.assertEquals(lectureLimit, successCount);
        Assertions.assertEquals(lectureLimit, successfulRegistrations);

        executorService.shutdown();
    }

    @Test
    @DisplayName("동시성 제어 순서 보장 테스트")
    void concurrent_LectureRegistration_order_test() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        List<Future<LectureRegistration>> futures = new ArrayList<>();

        for (User user : users) {
            futures.add(executorService.submit(() -> {
                LectureRegistration registration = LectureRegistration.builder()
                        .user(user)
                        .lecture(lecture)
                        .lrState(LectureRegistrationState.APPLY)
                        .build();
                return lectureRegistrationService.registerLecture(registration);
            }));
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // 결과 확인
        List<LectureRegistration> registrations = lectureRegistrationRepository.findAll();
        registrations.sort(Comparator.comparingInt(LectureRegistration::getLrSequence));

        for (int i = 0; i < registrations.size(); i++) {
            Assertions.assertEquals(i + 1, registrations.get(i).getLrSequence(), "순서 보장");
        }
    }

}
