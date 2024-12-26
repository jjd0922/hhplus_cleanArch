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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

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
        request.setUserIdx(user.getUIdx());
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
}
