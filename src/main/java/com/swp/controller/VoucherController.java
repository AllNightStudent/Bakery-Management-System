package com.swp.controller;

import com.swp.dto.VoucherView;
import com.swp.entity.CartEntity;
import com.swp.entity.CartItemEntity;
import com.swp.entity.Promotion;
import com.swp.repository.PromotionRepository;
import com.swp.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/vouchers")
public class VoucherController {

    private final UserService userService;
    private final CartService cartService;
    private final CartItemService cartItemService;
    private final PromotionService promotionService;
    private final PromotionRepository promotionRepository;

    /** Trang cho khách chọn voucher */
    @GetMapping("/select")
    public String showVoucherSelection(Model model, HttpSession session) {
        CartEntity cart = cartService.findCartByUser(userService.getCurrentUser());
        List<CartItemEntity> items = cartItemService.findAllByCart(cart);

        List<VoucherView> voucherViews = promotionService.getVoucherViews(items);

        // Lưu id voucher đang chọn (nếu có) từ session, để highlight
        Long selectedVoucherId = (Long) session.getAttribute("selectedVoucherId");

        model.addAttribute("voucherViews", voucherViews);
        model.addAttribute("selectedVoucherId", selectedVoucherId);

        return "voucher-select";
    }

    /** Áp dụng voucher: lưu vào session rồi quay lại trang thanh toán */
    @PostMapping("/apply")
    public String applyVoucher(@RequestParam("promotionId") Long promotionId,
                               @RequestParam("returnUrl") String returnUrl,
                               HttpSession session) {

        Promotion p = promotionRepository.findById(promotionId)
                .orElse(null);

        if (p != null) {
            session.setAttribute("selectedVoucherId", promotionId);
            session.setAttribute("selectedVoucherCode", p.getCode());  // ⭐ Lưu mã voucher
        }

        return "redirect:" + returnUrl;
    }


    /** Huỷ voucher đang chọn */
    @PostMapping("/clear")
    public String clearVoucher(@RequestParam("returnUrl") String returnUrl,
                               HttpSession session) {
        session.removeAttribute("selectedVoucherId");
        return "redirect:" + returnUrl;
    }
}
