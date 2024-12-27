package com.hanghe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghe.domain.lecture.entity.Lecture;
import com.hanghe.domain.lecture.repository.LectureRepository;
import com.hanghe.domain.lectureRegistration.entity.LectureRegistration;
import com.hanghe.domain.lectureRegistration.repository.LectureRegistrationRepository;
import com.hanghe.domain.lectureRegistration.state.LectureRegistrationState;
import com.hanghe.domain.user.entity.User;
import com.hanghe.domain.user.repository.UserRepository;
import com.hanghe.presentation.lectureRegistration.model.LectureRegistrationRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LectureRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LectureRepository lectureRepository;
    @Autowired
    private LectureRegistrationRepository lectureRegistrationRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRegisterLecture() throws Exception {
        // given
        User user = userRepository.save(
                User.builder()
                        .uId("testId")
                        .uName("테스트 유저")
                        .build()
        );

        Lecture lecture = lectureRepository.save(
                Lecture.builder()
                        .lTitle("TDD")
                        .lDescription("Learn TDD")
                        .lLecturer("강사")
                        .lDate(LocalDate.now())
                        .lLimit(30)
                        .build()
        );

        // 요청 데이터 생성
        LectureRegistrationRequest request = new LectureRegistrationRequest();
        request.setUserId(user.getUId());
        request.setLectureIdx(lecture.getLIdx());
        request.setLectureState(LectureRegistrationState.APPLY);

        // When: API 호출
        mockMvc.perform(post("/api/lecture-registration/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                // Then: 응답 검증
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lrIdx").exists())
                .andExpect(jsonPath("$.userId").value("testId"))
                .andExpect(jsonPath("$.userName").value("테스트 유저"))
                .andExpect(jsonPath("$.lectureIdx").value(lecture.getLIdx()))
                .andExpect(jsonPath("$.lectureTitle").value("TDD"))
                .andExpect(jsonPath("$.state").value("APPLY"));
    }

    @Test
    void test_selectLecture() throws Exception {
        // 데이터 준비
        User user = userRepository.save(
                User.builder()
                        .uId("testId")
                        .uName("테스트 유저")
                        .build()
        );
        Lecture lecture = lectureRepository.save(
                Lecture.builder()
                        .lTitle("TDD")
                        .lDescription("Learn TDD")
                        .lLecturer("강사")
                        .lDate(LocalDate.now())
                        .lLimit(30)
                        .build()
        );

        LectureRegistration registration1 = LectureRegistration.builder()
                .user(user)
                .lecture(lecture)
                .lrState(LectureRegistrationState.APPLY)
                .lrSequence(1)
                .build();

        LectureRegistration registration2 = LectureRegistration.builder()
                .user(user)
                .lecture(lecture)
                .lrState(LectureRegistrationState.APPLY)
                .lrSequence(2)
                .build();

        lectureRegistrationRepository.save(registration1);
        lectureRegistrationRepository.save(registration2);

        // MockMvc 요청 및 검증
        mockMvc.perform(get("/api/lecture-registration/user/" + user.getUIdx()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].lrIdx").exists())
                .andExpect(jsonPath("$[0].userIdx").value(user.getUIdx()))
                .andExpect(jsonPath("$[0].userId").value("testId"))
                .andExpect(jsonPath("$[0].userName").value("테스트 유저"))
                .andExpect(jsonPath("$[1].lrIdx").exists())
                .andExpect(jsonPath("$[1].userIdx").value(user.getUIdx()))
                .andExpect(jsonPath("$[1].userId").value("testId"))
                .andExpect(jsonPath("$[1].userName").value("테스트 유저"));
    }

    @Test
    @DisplayName("동시에 40명 신청, 30명 성공")
    void test_ConcurrentLecture_Registration_overLimit() throws Exception {
        // given
        Lecture lecture = lectureRepository.save(
                Lecture.builder()
                        .lTitle("TDD")
                        .lDescription("Learn TDD")
                        .lLecturer("강사")
                        .lDate(LocalDate.now())
                        .lLimit(30)
                        .build()
        );

        // 40명의 사용자 생성
        for (int i = 1; i <= 40; i++) {
            userRepository.save(
                    User.builder()
                            .uId("testUser" + i)
                            .uName("테스트 유저" + i)
                            .build()
            );
        }

        // when
        List<Thread> threads = new ArrayList<>();
        for (int i = 1; i <= 40; i++) {
            String userId = "testUser"+i;
            threads.add(new Thread(() -> {
                try {
                    LectureRegistrationRequest request = new LectureRegistrationRequest();
                    request.setUserId(userId);
                    request.setLectureIdx(lecture.getLIdx());
                    request.setLectureState(LectureRegistrationState.APPLY);

                    mockMvc.perform(post("/api/lecture-registration/register")
                                    .contentType("application/json")
                                    .content(objectMapper.writeValueAsString(request)))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        // 모든 스레드 시작
        threads.forEach(Thread::start);
        // 모든 스레드 종료 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // then: 최종적으로 30명만 신청 성공
        List<LectureRegistration> successfulRegistrations = lectureRegistrationRepository.findAllByLectureAndLrState(
                lecture, LectureRegistrationState.APPLY
        );

        Assertions.assertEquals(30, successfulRegistrations.size());

        // 신청 성공자의 신청 순서 검증
        for (int i = 0; i < successfulRegistrations.size(); i++) {
            Assertions.assertEquals(i + 1, successfulRegistrations.get(i).getLrSequence(),
                    "신청 순서는 1부터 차례로 증가해야 합니다.");
        }
    }

    @Test
    @DisplayName("한명이 같은 특강 5번 신청했을 때 1번 성공")
    void test_DuplicateLectureRegistration() throws Exception {
        // given
        User user = userRepository.save(
                User.builder()
                        .uId("testUser")
                        .uName("테스트 유저")
                        .build()
        );

        Lecture lecture = lectureRepository.save(
                Lecture.builder()
                        .lTitle("TDD")
                        .lDescription("Learn TDD")
                        .lLecturer("강사")
                        .lDate(LocalDate.now())
                        .lLimit(30)
                        .build()
        );

        LectureRegistrationRequest request = new LectureRegistrationRequest();
        request.setUserId(user.getUId());
        request.setLectureIdx(lecture.getLIdx());
        request.setLectureState(LectureRegistrationState.APPLY);

        // when: 동일한 요청을 5번 수행
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/lecture-registration/register")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        // then: 동일 유저와 동일 강의에 대한 신청은 1번만 성공
        List<LectureRegistration> registrations = lectureRegistrationRepository.findAllByLectureAndUserAndLrState(
                lecture, user, LectureRegistrationState.APPLY);

        Assertions.assertEquals(1, registrations.size(), "동일한 유저의 동일 강의 신청은 1번만 성공해야 합니다.");

        // 등록된 신청 데이터 검증
        LectureRegistration registration = registrations.get(0);
        Assertions.assertEquals(user.getUIdx(), registration.getUser().getUIdx(), "등록된 신청 데이터의 사용자 ID가 올바르지 않습니다.");
        Assertions.assertEquals(lecture.getLIdx(), registration.getLecture().getLIdx(), "등록된 신청 데이터의 강의 ID가 올바르지 않습니다.");
        Assertions.assertEquals(LectureRegistrationState.APPLY, registration.getLrState(), "등록된 신청 데이터의 상태가 올바르지 않습니다.");
    }
}
