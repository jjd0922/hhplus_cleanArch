package com.hanghe.presentation.lectureRegistration.controller;

import com.hanghe.domain.lecture.service.LectureService;
import com.hanghe.domain.lectureRegistration.entity.LectureRegistration;
import com.hanghe.domain.lectureRegistration.service.LectureRegistrationService;
import com.hanghe.domain.user.service.UserService;
import com.hanghe.presentation.lectureRegistration.model.LectureRegistrationRequest;
import com.hanghe.presentation.lectureRegistration.model.LectureRegistrationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/lecture-registration")
public class LectureRegistrationController {

    private final LectureRegistrationService lectureRegistrationService;
    private final LectureService lectureService;
    private final UserService userService;

    @PostMapping("/register")
    public LectureRegistrationResponse registerLecture(@RequestBody LectureRegistrationRequest request) {

        LectureRegistration registration = LectureRegistration.builder()
                .user(userService.findUserById(request.getUserIdx()).get())
                .lecture(lectureService.findLectureById(request.getLectureIdx()).get())
                .lrState(request.getLectureState())
                .build();

        LectureRegistration savedRegistration = lectureRegistrationService.registerLecture(registration);

        return LectureRegistrationResponse.builder()
                .lrIdx(savedRegistration.getLrIdx())
                .userId(savedRegistration.getUser().getUId())
                .userName(savedRegistration.getUser().getUName())
                .lectureIdx(savedRegistration.getLecture().getLIdx())
                .lectureTitle(savedRegistration.getLecture().getLTitle())
                .state(savedRegistration.getLrState())
                .createdAt(savedRegistration.getCreatedAt())
                .modifiedAt(savedRegistration.getModifiedAt())
                .build();
    }

    /** 유저의 신청 완료된 특강 목록 조회 */
    @GetMapping("/user/{id}")
    public List<LectureRegistrationResponse> selectLecture(@PathVariable("id") Long id) {
        return lectureRegistrationService.findLectureRegistrationListByUser(id).stream()
                .map(lectureRegistration -> LectureRegistrationResponse.builder()
                        .lrIdx(lectureRegistration.getLrIdx())
                        .userIdx(lectureRegistration.getUser().getUIdx())
                        .userId(lectureRegistration.getUser().getUId())
                        .userName(lectureRegistration.getUser().getUName())
                        .lectureIdx(lectureRegistration.getLecture().getLIdx())
                        .lectureTitle(lectureRegistration.getLecture().getLTitle())
                        .state(lectureRegistration.getLrState())
                        .createdAt(lectureRegistration.getCreatedAt())
                        .modifiedAt(lectureRegistration.getModifiedAt())
                        .build())
                .toList();
    }
}
