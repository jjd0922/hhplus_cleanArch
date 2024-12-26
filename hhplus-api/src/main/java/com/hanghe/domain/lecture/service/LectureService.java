package com.hanghe.domain.lecture.service;

import com.hanghe.domain.lecture.entity.Lecture;
import com.hanghe.domain.lecture.repository.LectureRepository;
import com.hanghe.domain.lectureRegistration.repository.LectureRegistrationRepository;
import com.hanghe.domain.lectureRegistration.state.LectureRegistrationState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureService {

    private final LectureRepository lectureRepository;
    private final LectureRegistrationRepository lectureRegistrationRepository;

    /** 강의 조회 */
    public Optional<Lecture> findLectureById(Long lectureId) {
        return lectureRepository.findById(lectureId);
    }

    /** 수강 가능한 강의 리스트 */
    public List<Lecture> findPossibleLectureList() {
        List<Lecture> possibleLecture = new ArrayList<>();
        List<Lecture> allLecture = lectureRepository.findAll();
        for (Lecture lecture : allLecture) {
            if(lectureRegistrationRepository.countByLectureAndLrState(lecture, LectureRegistrationState.APPLY) < lecture.getLLimit()){
                possibleLecture.add(lecture);
            }
        }
        return possibleLecture.stream().sorted(Comparator.comparing(Lecture :: getLIdx)).collect(Collectors.toList());
    }

}
