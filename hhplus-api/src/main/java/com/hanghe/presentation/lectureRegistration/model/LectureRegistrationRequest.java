package com.hanghe.presentation.lectureRegistration.model;

import com.hanghe.domain.lectureRegistration.state.LectureRegistrationState;
import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.misc.NotNull;

@Getter
@Setter
public class LectureRegistrationRequest {

    private Long userIdx;
    private Long lectureIdx;
    private LectureRegistrationState lectureState;

}
