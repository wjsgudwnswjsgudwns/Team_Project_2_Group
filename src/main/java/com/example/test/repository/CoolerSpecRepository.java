package com.example.test.repository;

import com.example.test.entity.CoolerSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoolerSpecRepository extends JpaRepository<CoolerSpec, Long> {
}