package com.swp.controller;

import com.swp.dto.ReviewCreateRequest;
import com.swp.entity.OrderItemEntity;
import com.swp.entity.Review;
import com.swp.entity.UserEntity;
import com.swp.repository.OrderItemRepository;
import com.swp.repository.ReviewMediaRepository;
import com.swp.repository.ReviewReplyRepository;
import com.swp.repository.ReviewRepository;
import com.swp.repository.UserRepository;
import com.swp.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepo;
    private final UserRepository userRepository;
    private final ReviewMediaRepository mediaRepo;
    private final ReviewReplyRepository replyRepo;

    /** ========================= NEW FORM ========================= **/
    @GetMapping("/new")
    public String newForm(@PathVariable Long productId,
                          @RequestParam(required = false) Long orderItemId,
                          Authentication auth,
                          Model model,
                          RedirectAttributes ra) {

        if (auth == null || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user"));

        OrderItemEntity oi = null;
        if (orderItemId != null) {
            oi = orderItemRepo.findById(orderItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Order item not found"));



            if (reviewRepository.existsByOrderItemAndUser(oi, user)) {
                ra.addFlashAttribute("info", "Bạn đã đánh giá sản phẩm này rồi.");
                return "redirect:/orders/" + oi.getOrder().getOrderId() + "/details";
            }
        }

        model.addAttribute("productId", productId);
        model.addAttribute("orderItemId", orderItemId);
        model.addAttribute("req", new ReviewCreateRequest(0, "", "", orderItemId, false));
        return "write-reviews";
    }

    /** ========================= CREATE REVIEW ========================= **/
    @PostMapping
    public String create(@PathVariable Long productId,
                         @Valid @ModelAttribute("req") ReviewCreateRequest req,
                         BindingResult br,
                         @RequestParam(name="photos", required=false) List<MultipartFile> photos,
                         Authentication auth,
                         RedirectAttributes ra) throws IOException {


        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email).orElse(null);




        Long orderId = null;
        if (req.orderItemId() != null) {
            var oi = orderItemRepo.findById(req.orderItemId()).orElse(null);
            if (oi != null) {
                orderId = oi.getOrder().getOrderId();
            }
        }

        reviewService.createReview(productId, user, req, photos);
        ra.addFlashAttribute("success", "Đã gửi đánh giá!");

        if (orderId != null) {
            return "redirect:/orders/" + orderId + "/details";
        }
        return "redirect:/products/" + productId;
    }

    /** ========================= VIEW MY REVIEW ========================= **/
    @GetMapping("/view")
    public String viewMyReview(@PathVariable Long productId,
                               @RequestParam Long orderItemId,
                               Authentication auth,
                               RedirectAttributes ra,
                               Model model) {

        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user"));

        OrderItemEntity oi = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found"));



        var optReview = reviewRepository.findByOrderItemAndUser(oi, user);
        if (optReview.isEmpty()) {
            // chưa có review -> đẩy sang màn new
            ra.addFlashAttribute("info", "Bạn chưa đánh giá sản phẩm này. Hãy viết đánh giá mới.");
            return "redirect:/orders/" + oi.getOrder().getOrderId() + "/details";
        }

        Review review = optReview.get();

        var media   = mediaRepo.findByReviewOrderByMediaIdAsc(review);
        var replies = replyRepo.findByReviewOrderByCreatedAtAsc(review);

        model.addAttribute("r", review);
        model.addAttribute("media", media);
        model.addAttribute("replies", replies);
        model.addAttribute("productId", productId);
        model.addAttribute("orderItemId", orderItemId);
        model.addAttribute("orderId", oi.getOrder().getOrderId());

        return "my-review";  // <-- template xem chi tiết review
    }
}
