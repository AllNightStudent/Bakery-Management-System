// ReviewReplyRepository.java
package com.swp.repository;

import com.swp.entity.Review;
import com.swp.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    List<ReviewReply> findByReviewOrderByCreatedAtAsc(Review review);
}
