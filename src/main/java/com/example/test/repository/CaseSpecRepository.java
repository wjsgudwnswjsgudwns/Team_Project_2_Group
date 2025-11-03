package com.example.test.repository;

import com.example.test.entity.CaseSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseSpecRepository extends JpaRepository<CaseSpec, Long> {
}