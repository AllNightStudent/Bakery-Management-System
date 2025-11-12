package com.swp.repository;

import com.swp.entity.Review;
import com.swp.entity.ReviewMedia;
import com.swp.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewMediaRepository extends JpaRepository<ReviewMedia, Long> {

    List<ReviewMedia> findByReviewOrderByMediaIdAsc(Review review);
}

