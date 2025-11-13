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

            if (!oi.getOrder().getUser().getId().equals(user.getId())) {
                ra.addFlashAttribute("error", "Sản phẩm này không thuộc đơn hàng của bạn.");
                return "redirect:/orders/" + oi.getOrder().getOrderId() + "/details";
            }

            // Nếu đã review rồi -> quay về chi tiết review luôn
            if (reviewRepository.existsByOrderItemAndUser(oi, user)) {
                return "redirect:/products/" + productId + "/reviews/view?orderItemId=" + orderItemId;
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

        System.out.println("[REVIEW POST] principal="
                + (auth == null ? null : auth.getName())
                + ", isAuth=" + (auth != null && auth.isAuthenticated())
                + ", anon=" + (auth instanceof AnonymousAuthenticationToken));


        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email).orElse(null);


        if (br.hasErrors()) {
            br.getFieldErrors().forEach(e ->
                    System.out.printf("[REVIEW ERR] field=%s, rejected=%s, msg=%s%n",
                            e.getField(), e.getRejectedValue(), e.getDefaultMessage())
            );
            ra.addFlashAttribute("org.springframework.validation.BindingResult.req", br);
            ra.addFlashAttribute("req", req);
            ra.addFlashAttribute("error", "Vui lòng kiểm tra lại các trường nhập.");
            return "redirect:/products/" + productId + "/reviews/new?orderItemId=" + req.orderItemId();
        }

        Long orderId = null;
        if (req.orderItemId() != null) {
            var oi = orderItemRepo.findById(req.orderItemId()).orElse(null);
            if (oi != null) {
                orderId = oi.getOrder().getOrderId();
            }
        }

        reviewService.createReview(productId, user, req, photos);
        ra.addFlashAttribute("success", "Đã gửi đánh giá! Sẽ hiển thị sau khi được duyệt.");

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

        System.out.println("[REVIEW VIEW] productId=" + productId + ", orderItemId=" + orderItemId);

        if (auth == null || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user"));

        OrderItemEntity oi = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found"));

        if (!oi.getOrder().getUser().getId().equals(user.getId())) {
            ra.addFlashAttribute("error", "Mục đơn hàng không thuộc về bạn.");
            return "redirect:/orders/" + oi.getOrder().getOrderId() + "/details";
        }

        var optReview = reviewRepository.findByOrderItemAndUser(oi, user);
        if (optReview.isEmpty()) {
            // chưa có review -> đẩy sang màn new
            ra.addFlashAttribute("info", "Bạn chưa đánh giá sản phẩm này. Hãy viết đánh giá mới.");
            return "redirect:/products/" + productId + "/reviews/new?orderItemId=" + orderItemId;
        }

        Review review = optReview.get();
        System.out.println("[REVIEW VIEW] found reviewId=" + review.getReviewId());

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
