package com.example.test.repository;

import com.example.test.entity.MemorySpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemorySpecRepository extends JpaRepository<MemorySpec, Long> {
}