package com.example.test.repository;

import com.example.test.entity.CpuSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CpuSpecRepository extends JpaRepository<CpuSpec, Long> {
}