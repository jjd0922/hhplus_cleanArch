package com.hanghe.domain.lectureRegistration.state;

import com.hanghe.domain.base.EnumDescription;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LectureRegistrationState implements EnumDescription {

    APPLY("신청"),
    CANCEL("취소");

    private final String description;

}
