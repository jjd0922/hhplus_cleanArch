package com.hanghe.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanghe.domain.lecture.entity.Lecture;
import com.hanghe.domain.lecture.service.LectureService;
import com.hanghe.presentation.lecture.controller.LectureController;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LectureController.class)
public class LectureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LectureService lectureService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void test_saerch_Lecture() throws Exception {
        // Mock Lecture 데이터 생성
        Lecture mockLecture = Lecture.builder()
                .lIdx(1L)
                .lTitle("TDD")
                .lDescription("Learn TDD")
                .lLecturer("강사")
                .lDate(LocalDate.now())
                .lLimit(30)
                .build();

        // Mock 동작 설정
        Mockito.when(lectureService.findLectureById(anyLong())).thenReturn(Optional.of(mockLecture));

        // MockMvc를 사용한 요청 및 검증
        mockMvc.perform(get("/api/lecture/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("TDD"))
                .andExpect(jsonPath("$.description").value("Learn TDD"))
                .andExpect(jsonPath("$.lecturer").value("강사"))
                .andExpect(jsonPath("$.lectureDate").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.lectureLimit").value(30));

        // Mock 동작 호출 확인
        Mockito.verify(lectureService, times(1)).findLectureById(1L);
    }

    @Test
    void test_selectPossibleLecture() throws Exception {
        // Mock 데이터 생성
        List<Lecture> mockLectures = Arrays.asList(
                Lecture.builder()
                        .lIdx(1L)
                        .lTitle("TDD")
                        .lDescription("Learn TDD")
                        .lLecturer("강사1")
                        .lDate(LocalDate.of(2024, 12, 27))
                        .lLimit(30)
                        .build(),
                Lecture.builder()
                        .lIdx(2L)
                        .lTitle("Clean Architecture")
                        .lDescription("Learn Clean Architecture")
                        .lLecturer("강사2")
                        .lDate(LocalDate.of(2024, 12, 27))
                        .lLimit(30)
                        .build()
        );

        // Mock 동작 설정
        Mockito.when(lectureService.findPossibleLectureList()).thenReturn(mockLectures);

        // MockMvc를 사용한 GET 요청 및 검증
        mockMvc.perform(get("/api/lecture/possible-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("TDD"))
                .andExpect(jsonPath("$[0].description").value("Learn TDD"))
                .andExpect(jsonPath("$[0].lecturer").value("강사1"))
                .andExpect(jsonPath("$[0].lectureDate").value("2024-12-27"))
                .andExpect(jsonPath("$[0].lectureLimit").value(30))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Clean Architecture"))
                .andExpect(jsonPath("$[1].description").value("Learn Clean Architecture"))
                .andExpect(jsonPath("$[1].lecturer").value("강사2"))
                .andExpect(jsonPath("$[1].lectureDate").value("2024-12-27"))
                .andExpect(jsonPath("$[1].lectureLimit").value(30));

        // Service 호출 검증
        Mockito.verify(lectureService, times(1)).findPossibleLectureList();
    }


}
