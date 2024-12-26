package com.hanghe.presentation.lectureRegistration.model;

import com.hanghe.domain.lectureRegistration.state.LectureRegistrationState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LectureRegistrationResponse {

    private Long lrIdx;
    private Long userIdx;
    private String userId;
    private String userName;
    private Long lectureIdx;
    private String lectureTitle;
    private LectureRegistrationState state;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

}
