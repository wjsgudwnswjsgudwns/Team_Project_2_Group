package com.example.test.repository;

import com.example.test.entity.Help;
import com.example.test.entity.HelpAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HelpAnswerRepository extends JpaRepository<HelpAnswer, Long> {

    Optional<HelpAnswer> findByHelp(Help help);
}
