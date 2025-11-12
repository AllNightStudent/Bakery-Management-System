// AdminReviewController.java
package com.swp.controller.admin;

import com.swp.entity.Review;
import com.swp.entity.ReviewMedia;
import com.swp.entity.ReviewReply;
import com.swp.entity.enums.ReviewStatus;
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
    public String list(@RequestParam(required = false) ReviewStatus status,
                       @RequestParam(required = false) Long productId,
                       @RequestParam(required = false) Integer stars,
                       @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model,
                       RedirectAttributes ra) {
        try {
            Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by("createdAt").descending());
            Page<Review> data = reviewRepo.search(status, productId, stars, (q == null || q.isBlank() ? null : q), pageable);

            model.addAttribute("data", data);
            model.addAttribute("status", status);
            model.addAttribute("productId", productId);
            model.addAttribute("stars", stars);
            model.addAttribute("q", q);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            model.addAttribute("statuses", ReviewStatus.values());
            return "admin/reviews"; // <— đổi đúng file bạn có
        } catch (Exception e) {
            System.out.println("Error: " + e);
            e.printStackTrace(); // xem stacktrace trong console
            ra.addFlashAttribute("error", "Không tải được danh sách review: " + e.getClass().getSimpleName());
            return "redirect:/"; // hoặc trả về 1 trang admin home
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

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        Review r = reviewRepo.findById(id).orElseThrow();
        r.setStatus(ReviewStatus.APPROVED);
        reviewRepo.save(r);
        ra.addFlashAttribute("success", "Đã duyệt review #" + id);
        return "redirect:/admin/reviews";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        Review r = reviewRepo.findById(id).orElseThrow();
        r.setStatus(ReviewStatus.REJECTED);
        reviewRepo.save(r);
        ra.addFlashAttribute("success", "Đã từ chối review #" + id);
        return "redirect:/admin/reviews";
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
