package com.hanghe.presentation.lecture.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LectureRequest {

    private long id;
    private String title;
    private String description;
    private String lecturer;
    private LocalDate lectureDate;
    private int lectureLimit = 30;
}
