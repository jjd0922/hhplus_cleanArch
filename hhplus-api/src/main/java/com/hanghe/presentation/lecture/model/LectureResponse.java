package com.hanghe.presentation.lecture.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LectureResponse {

    private Long id;
    private String title;
    private String description;
    private String lecturer;
    private LocalDate lectureDate;
    private int lectureLimit;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
