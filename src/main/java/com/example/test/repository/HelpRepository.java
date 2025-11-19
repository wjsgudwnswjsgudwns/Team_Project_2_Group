package com.example.test.repository;

import com.example.test.entity.Help;
import com.example.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HelpRepository extends JpaRepository<Help, Long> {

    List<Help> findByUserOrderByInquiryDateDesc(User user); // 유저 문의 조회, 최신순
    List<Help> findByIsAnsweredOrderByInquiryDateDesc(boolean isAnswered);
    List<Help> findAllByOrderByInquiryDateDesc();

    List<Help> findByNameAndPhoneAndUserIsNullOrderByInquiryDateDesc(String name, String phone); // 비회원 문의 조회

    @Query("SELECT h FROM Help h " +
            "LEFT JOIN FETCH h.helpAnswer ha " +
            "LEFT JOIN FETCH ha.admin " +
            "LEFT JOIN FETCH h.user " +
            "WHERE h.id = :id")
    Optional<Help> findByIdWithAnswer(@Param("id") Long id);
}
