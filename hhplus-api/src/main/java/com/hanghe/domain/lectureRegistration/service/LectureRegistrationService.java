package com.hanghe.domain.lectureRegistration.service;

import com.hanghe.domain.lecture.entity.Lecture;
import com.hanghe.domain.lecture.repository.LectureRepository;
import com.hanghe.domain.lectureRegistration.entity.LectureRegistration;
import com.hanghe.domain.lectureRegistration.repository.LectureRegistrationRepository;
import com.hanghe.domain.lectureRegistration.state.LectureRegistrationState;
import com.hanghe.domain.user.entity.User;
import com.hanghe.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureRegistrationService {
    private final LectureRegistrationRepository lectureRegistrationRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    /** 강의 신청 */
    public LectureRegistration registerLecture(LectureRegistration lectureRegistration) {

        // 강의에 락 설정하여 동시 접근 방지
        Lecture lecture = lectureRepository.findWithLockByLIdx(lectureRegistration.getLecture().getLIdx())
                .orElseThrow(() -> new RuntimeException("해당 강의는 조회되지 않습니다."));

        if(lectureRegistrationRepository.findByUserAndLectureAndLrState(lectureRegistration.getUser(),lectureRegistration.getLecture(),LectureRegistrationState.APPLY) != null){
            throw new RuntimeException("이미 신청완료된 유저입니다.");
        }
        if(lectureRegistrationRepository.countByLectureAndLrState(lectureRegistration.getLecture(),LectureRegistrationState.APPLY) >= lecture.getLLimit()){
            this.setLectureRegistrationCancelWhenOverLimit(lecture);
            throw new RuntimeException("수강 신청 인원이 가득찼습니다.");
        }

        // 현재 최대 sequence 값 조회
        int maxSequence = lectureRegistrationRepository.findMaxSequenceByLecture(lecture);
        // sequence 설정
        lectureRegistration.setLrSequence(maxSequence + 1);

        // 해당 강의 취소 유저 재신청
        LectureRegistration cancel_lectureRegistration = lectureRegistrationRepository.findByUserAndLectureAndLrState(lectureRegistration.getUser(),lectureRegistration.getLecture(),LectureRegistrationState.CANCEL);
        if(cancel_lectureRegistration != null){
            cancel_lectureRegistration.setLrState(LectureRegistrationState.APPLY);
            return cancel_lectureRegistration;
        }
        return lectureRegistrationRepository.save(lectureRegistration);
    }

    /** 강의 초과 신청된 데이터 취소처리 */
    public void setLectureRegistrationCancelWhenOverLimit(Lecture lecture){
        List<LectureRegistration> cancelList = lectureRegistrationRepository.findAllByLectureAndLrStateAndLrSequenceGreaterThan(lecture,LectureRegistrationState.APPLY,lecture.getLLimit());
        for (LectureRegistration lectureRegistration : cancelList) {
            if(lectureRegistration !=null){
                lectureRegistration.setLrState(LectureRegistrationState.CANCEL);
            }
        }
    }

    /** 유저의 신청 완료된 특강 목록 조회 */
    public List<LectureRegistration> findLectureRegistrationListByUser(Long userId) {
        User user = userRepository.findById(userId).get();
        return lectureRegistrationRepository.findAllByUserAndLrState(user,LectureRegistrationState.APPLY).stream().sorted(Comparator.comparing(LectureRegistration :: getLrIdx)).collect(Collectors.toList());
    }
}
