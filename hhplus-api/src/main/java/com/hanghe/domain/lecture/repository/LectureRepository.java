package com.hanghe.domain.lecture.repository;

import com.hanghe.domain.lecture.entity.Lecture;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LectureRepository extends JpaRepository<Lecture,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lecture l WHERE l.lIdx = :lIdx")
    Optional<Lecture> findWithLockByLIdx(@Param("lIdx") Long lIdx);
}
