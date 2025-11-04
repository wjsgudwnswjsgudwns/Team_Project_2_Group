package com.example.test.repository;

import com.example.test.entity.GpuSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpuSpecRepository extends JpaRepository<GpuSpec, Long> {
}