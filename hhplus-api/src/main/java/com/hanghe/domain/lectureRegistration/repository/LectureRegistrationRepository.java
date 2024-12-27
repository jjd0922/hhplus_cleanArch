package com.hanghe.domain.lectureRegistration.repository;

import com.hanghe.domain.lecture.entity.Lecture;
import com.hanghe.domain.lectureRegistration.entity.LectureRegistration;
import com.hanghe.domain.lectureRegistration.state.LectureRegistrationState;
import com.hanghe.domain.user.entity.User;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureRegistrationRepository extends JpaRepository<LectureRegistration,Long> {

    /** 강의 신청 조회*/
    LectureRegistration findByUserAndLectureAndLrState(User user, Lecture lecture, LectureRegistrationState state);

    /** 유저 신청 강의 목록 조회*/
    List<LectureRegistration> findAllByUserAndLrState(User user, LectureRegistrationState state);

    /** 강의 신칭 유저 목록 조회*/
    List<LectureRegistration> findAllByLectureAndLrState(Lecture lecture, LectureRegistrationState state);

    /** 강의 등록인원 수 조회*/
    int countByLectureAndLrState(Lecture lecture, LectureRegistrationState state);

    /** 강의 신청 max sequence */
    @Query("SELECT COALESCE(MAX(r.lrSequence), 0) FROM LectureRegistration r WHERE r.lecture = :lecture")
    int findMaxSequenceByLecture(@Param("lecture") Lecture lecture);

    /** 강의 신청 초과 데이터 조회 */
    List<LectureRegistration> findAllByLectureAndLrStateAndLrSequenceGreaterThan(Lecture lecture, LectureRegistrationState state, int limit);

    @EntityGraph(attributePaths = {"user", "lecture"})
    List<LectureRegistration> findAllByLectureAndUserAndLrState(Lecture lecture,User user,LectureRegistrationState state);
}
