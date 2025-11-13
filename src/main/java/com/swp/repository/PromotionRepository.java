package com.swp.repository;

import com.swp.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("""
           SELECT p FROM Promotion p
           WHERE p.active = true
             AND (p.startDate IS NULL OR p.startDate <= :now)
             AND (p.endDate IS NULL OR p.endDate >= :now)
           """)
    List<Promotion> findAllActive(LocalDateTime now);
}
