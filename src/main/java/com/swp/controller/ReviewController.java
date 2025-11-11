package com.swp.controller;

import com.swp.dto.ReviewCreateRequest;
import com.swp.entity.UserEntity;
import com.swp.repository.OrderItemRepository;
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

    /** Trang form riêng: GET /products/{productId}/reviews/new */
    // ReviewController.java
    @GetMapping("/new")
    public String newForm(@PathVariable Long productId,
                          @RequestParam(required = false) Long orderItemId,
                          Authentication auth,
                          Model model,
                          RedirectAttributes ra) { // <-- thêm RedirectAttributes

        if (auth == null || !auth.isAuthenticated()
                || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user"));

        // Nếu có orderItemId thì kiểm tra đã review chưa
        if (orderItemId != null) {
            var oi = orderItemRepo.findById(orderItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Order item not found"));

            // bảo vệ: item phải thuộc user
            if (!oi.getOrder().getUser().getId().equals(user.getId())) {
                ra.addFlashAttribute("error", "Sản phẩm này không thuộc đơn hàng của bạn.");
                return "redirect:/orders/" + oi.getOrder().getOrderId() + "/details";
            }

            // 👉 THÔNG BÁO SỚM nếu đã review
            if (reviewRepository.existsByOrderItemAndUser(oi, user)) {
                ra.addFlashAttribute("info", "Bạn đã đánh giá sản phẩm này rồi.");
                return "redirect:/orders/" + oi.getOrder().getOrderId() + "/details";
            }
        }

        // chưa review -> hiển thị form như bình thường
        model.addAttribute("eligibleOrderItems",
                orderItemRepo.findDeliveredByUserAndProduct(user.getId(), productId));
        model.addAttribute("productId", productId);
        model.addAttribute("orderItemId", orderItemId);
        model.addAttribute("req", new ReviewCreateRequest(0, "", "", orderItemId, false));

        return "write-reviews"; // view của bạn
    }


    /** Submit form: POST /products/{productId}/reviews */
    @PostMapping
    public String create(@PathVariable Long productId,
                         @Valid @ModelAttribute("req") ReviewCreateRequest req,
                         BindingResult br,
                         @RequestParam(name="photos", required=false) List<MultipartFile> photos,
                         Authentication auth,
                         RedirectAttributes ra) throws IOException {

        System.out.println("[REVIEW POST] principal="
                + (auth==null?null:auth.getName())
                + ", isAuth=" + (auth!=null && auth.isAuthenticated())
                + ", anon=" + (auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken));

        // 1) Bảo vệ đăng nhập
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để viết đánh giá.");
            return "redirect:/login";
        }
        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            ra.addFlashAttribute("error", "Tài khoản không hợp lệ.");
            return "redirect:/login";
        }

        // 2) Nếu form lỗi → quay lại trang Product (giữ như cũ)
        if (br.hasErrors()) {
            br.getFieldErrors().forEach(e ->
                    System.out.printf("[REVIEW ERR] field=%s, rejected=%s, msg=%s%n",
                            e.getField(), e.getRejectedValue(), e.getDefaultMessage())
            );
            ra.addFlashAttribute("org.springframework.validation.BindingResult.req", br);
            ra.addFlashAttribute("req", req);
            ra.addFlashAttribute("error", "Vui lòng kiểm tra lại các trường nhập.");
            return "redirect:/products/" + productId + "?writeReview=1"
                    + (req.orderItemId()!=null ? "&orderItemId="+req.orderItemId() : "");
        }

        // 3) Lấy orderId trước khi tạo review để biết chỗ quay về
        Long orderId = null;
        if (req.orderItemId() != null) {
            var oi = orderItemRepo.findById(req.orderItemId())
                    .orElse(null);
            if (oi != null) {
                orderId = oi.getOrder().getOrderId(); // đổi getter theo entity của bạn
            }
        }

        // 4) Tạo review
        reviewService.createReview(productId, user, req, photos);

        ra.addFlashAttribute("success", "Đã gửi đánh giá! Sẽ hiển thị sau khi được duyệt.");
            System.out.println("Order ID không lỗi");
            return "redirect:/orders/" + orderId+"/details";   // <--- chỉnh đúng route chi tiết đơn của bạn
    }

}
