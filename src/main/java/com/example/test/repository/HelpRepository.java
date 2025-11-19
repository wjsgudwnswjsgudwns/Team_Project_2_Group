package com.example.test.repository;

import com.example.test.entity.Help;
import com.example.test.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HelpRepository extends JpaRepository<Help, Long> {

    Page<Help> findByUser(User user, Pageable pageable);
    Page<Help> findByNameAndPhoneAndUserIsNull(String name, String phone, Pageable pageable);

//    // 기존 메서드 (하위 호환성)
//    List<Help> findByUserOrderByInquiryDateDesc(User user);
//    List<Help> findByIsAnsweredOrderByInquiryDateDesc(boolean isAnswered);
//    List<Help> findAllByOrderByInquiryDateDesc();
//    List<Help> findByNameAndPhoneAndUserIsNullOrderByInquiryDateDesc(String name, String phone);

    @Query("SELECT h FROM Help h " +
            "LEFT JOIN FETCH h.helpAnswer ha " +
            "LEFT JOIN FETCH ha.admin " +
            "LEFT JOIN FETCH h.user " +
            "WHERE h.id = :id")
    Optional<Help> findByIdWithAnswer(@Param("id") Long id);
}