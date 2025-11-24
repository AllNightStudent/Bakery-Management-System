package com.swp.service;

import com.swp.dto.ReviewCreateRequest;
import com.swp.entity.*;
import com.swp.repository.OrderItemRepository;
import com.swp.repository.ProductRepository;
import com.swp.repository.ReviewMediaRepository;
import com.swp.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ProductRepository productRepo;
    private final OrderItemRepository orderItemRepo;
    private final ReviewRepository reviewRepo;
    private final ReviewMediaRepository mediaRepo;
    private final LocalStorageService storage;

    /**
     * Kiểm tra quyền & tạo review. Ảnh sẽ được lưu sau khi có reviewId
     */
    @Transactional
    public Long createReview(Long productId, UserEntity user, ReviewCreateRequest req,
                             List<MultipartFile> photos) throws IOException {

        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        OrderItemEntity orderItem = null;


        reviewRepo.findByOrderItemAndUser(orderItem, user)
                .ifPresent(r -> {
                    throw new IllegalStateException("You already reviewed this item");
                });


        Review review = Review.builder()
                .product(product)
                .user(user)
                .orderItem(orderItem)
                .rating(req.rating())
                .title(req.title())
                .content(req.content())
                .build();

        review = reviewRepo.save(review);

        if (photos != null) {
            int max = Math.min(photos.size(), 6);
            for (int i = 0; i < max; i++) {
                MultipartFile f = photos.get(i);
                if (f.isEmpty() || !f.getContentType().startsWith("image/")) continue;
                String url = storage.saveReviewImage(review.getReviewId(), f);
                mediaRepo.save(ReviewMedia.builder()
                        .review(review)
                        .url(url)
                        .mediaType("image")
                        .build());
            }
        }

        return review.getReviewId();
    }
}

