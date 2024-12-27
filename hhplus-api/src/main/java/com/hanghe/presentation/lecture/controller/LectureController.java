package com.hanghe.presentation.lecture.controller;

import com.hanghe.domain.lecture.entity.Lecture;
import com.hanghe.domain.lecture.service.LectureService;
import com.hanghe.presentation.lecture.model.LectureRequest;
import com.hanghe.presentation.lecture.model.LectureResponse;
import com.hanghe.presentation.lectureRegistration.model.LectureRegistrationRequest;
import com.hanghe.presentation.lectureRegistration.model.LectureRegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/lecture")
public class LectureController {

    private final LectureService lectureService;

    /** 강의 조회 */
    @GetMapping("/{id}")
    public LectureResponse selectLecture(@PathVariable("id") Long id) {
        Lecture lecture = lectureService.findLectureById(id).get();
        return LectureResponse.builder()
                .id(lecture.getLIdx())
                .title(lecture.getLTitle())
                .description(lecture.getLDescription())
                .lecturer(lecture.getLLecturer())
                .lectureDate(lecture.getLDate())
                .lectureLimit(lecture.getLLimit())
                .createdAt(lecture.getCreatedAt())
                .modifiedAt(lecture.getModifiedAt())
                .build();
    }

    /** 수강 가능한 강의 리스트 */
    @GetMapping("/possible-list")
    public List<LectureResponse> selectPossibleLecture() {

        return lectureService.findPossibleLectureList().stream()
                .map(lecture -> LectureResponse.builder()
                        .id(lecture.getLIdx())
                        .title(lecture.getLTitle())
                        .description(lecture.getLDescription())
                        .lecturer(lecture.getLLecturer())
                        .lectureDate(lecture.getLDate())
                        .lectureLimit(lecture.getLLimit())
                        .createdAt(lecture.getCreatedAt())
                        .modifiedAt(lecture.getModifiedAt())
                        .build())
                .toList();
    }
}
