package com.example.test.repository;

import com.example.test.entity.Help;
import com.example.test.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelpRepository extends JpaRepository<Help, Long> {

    List<Help> findByUserOrderByInquiryDateDesc(User user); // 유저 문의 조회, 최신순
    List<Help> findByIsAnsweredOrderByInquiryDateDesc(boolean isAnswered);
    List<Help> findAllByOrderByInquiryDateDesc();

    List<Help> findByNameAndPhoneAndUserIsNullOrderByInquiryDateDesc(String name, String phone); // 비회원 문의 조회
}
