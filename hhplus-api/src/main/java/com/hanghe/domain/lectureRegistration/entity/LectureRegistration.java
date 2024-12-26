package com.hanghe.domain.lectureRegistration.entity;

import com.hanghe.domain.lecture.entity.Lecture;
import com.hanghe.domain.lectureRegistration.state.LectureRegistrationState;
import com.hanghe.domain.user.entity.User;
import com.hanghe.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicInsert
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LectureRegistration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lrIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LR_U_IDX", referencedColumnName = "uIdx")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LR_L_IDX", referencedColumnName = "lIdx")
    private Lecture lecture;

    @Enumerated(EnumType.STRING)
    private LectureRegistrationState lrState;

    @Column(nullable = false)
    private int lrSequence; // 순서 관리 필드

}
