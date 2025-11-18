package com.example.test.repository;

import com.example.test.entity.HelpAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HelpAnswerRepository extends JpaRepository<HelpAnswer, Long> {
}
