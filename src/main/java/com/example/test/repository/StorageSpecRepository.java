package com.example.test.repository;

import com.example.test.entity.StorageSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorageSpecRepository extends JpaRepository<StorageSpec, Long> {
}