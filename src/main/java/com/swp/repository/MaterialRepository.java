package com.swp.repository;

import com.swp.entity.MaterialEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, Long> {

	@Query("SELECT m FROM MaterialEntity m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	Page<MaterialEntity> searchByName(@Param("keyword") String keyword, Pageable pageable);

	boolean existsByName(String name);
}


