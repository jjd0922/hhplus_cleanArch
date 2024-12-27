package com.hanghe.domain.user.repository;

import com.hanghe.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Long> {
    @Query("SELECT u FROM User u WHERE u.uId = :uId")
    User findUserByUId(@Param("uId") String uId);
}
