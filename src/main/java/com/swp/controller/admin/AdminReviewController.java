// AdminReviewController.java
package com.swp.controller.admin;

import com.swp.entity.Review;
import com.swp.entity.ReviewMedia;
import com.swp.entity.ReviewReply;
import com.swp.repository.ReviewMediaRepository;
import com.swp.repository.ReviewReplyRepository;
import com.swp.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewRepository reviewRepo;
    private final ReviewReplyRepository replyRepo;
    private final ReviewMediaRepository mediaRepo;

    @GetMapping
    public String list(@RequestParam(required = false) Integer stars,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model,
                       RedirectAttributes ra) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by("createdAt").descending());
            Page<Review> data = reviewRepo.search(
                    stars,
                    (keyword == null || keyword.isBlank() ? null : keyword),
                    pageable
            );

            model.addAttribute("data", data);
            model.addAttribute("stars", stars);
            model.addAttribute("keyword", keyword);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            return "admin/reviews";
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace();
            ra.addFlashAttribute("error", "Không tải được danh sách review: " + e.getClass().getSimpleName());
            return "redirect:/";
        }
    }


    // AdminReviewController.java
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Review review = reviewRepo.findById(id).orElseThrow();
        List<ReviewMedia> media = mediaRepo.findByReviewOrderByMediaIdAsc(review);
        List<ReviewReply> replies = replyRepo.findByReviewOrderByCreatedAtAsc(review);

        model.addAttribute("r", review);
        model.addAttribute("media", media);
        model.addAttribute("replies", replies);
        return "admin/reviewsdetail";
    }



    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id,
                        @RequestParam String content,
                        @RequestParam(defaultValue = "true") boolean isPublic,
                        Authentication auth,
                        RedirectAttributes ra) {
        Review r = reviewRepo.findById(id).orElseThrow();
        if (content != null && !content.isBlank()) {
            ReviewReply reply = ReviewReply.builder()
                    .review(r)
                    .content(content.trim())
                    .isPublic(isPublic)
                    .adminEmail(auth != null ? auth.getName() : "admin")
                    .build();
            replyRepo.save(reply);
            ra.addFlashAttribute("success", "Đã phản hồi review #" + id);
        } else {
            ra.addFlashAttribute("error", "Nội dung phản hồi không được để trống.");
        }
        return "redirect:/admin/reviews";
    }
}
