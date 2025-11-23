package com.swp.controller.admin;

import com.swp.entity.Promotion;
import com.swp.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    private final PromotionService promotionService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", promotionService.findAll());
        return "admin/voucher-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        Promotion voucher = new Promotion();
        // Có thể set startDate mặc định là bây giờ, endDate null
        voucher.setStartDate(LocalDateTime.now());
        model.addAttribute("voucher", voucher);
        return "admin/voucher-form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("voucher") Promotion voucher,
                         RedirectAttributes redirectAttributes) {
        promotionService.save(voucher);
        redirectAttributes.addFlashAttribute("success", "Tạo voucher thành công!");
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Promotion voucher = promotionService.findByIdOrThrow(id);
        model.addAttribute("voucher", voucher);
        return "admin/voucher-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("voucher") Promotion form,
                         RedirectAttributes redirectAttributes) {

        Promotion existing = promotionService.findByIdOrThrow(id);

        existing.setCode(form.getCode());
        existing.setName(form.getName());
        existing.setDescription(form.getDescription());
        existing.setDiscountType(form.getDiscountType());
        existing.setDiscountValue(form.getDiscountValue());
        existing.setMinOrderValue(form.getMinOrderValue());
        existing.setMaxOrderValue(form.getMaxOrderValue());
        existing.setMinTotalQuantity(form.getMinTotalQuantity());
        existing.setProductScopeType(form.getProductScopeType());
        existing.setCategories(form.getCategories());
        existing.setStartDate(form.getStartDate());
        existing.setEndDate(form.getEndDate());

        promotionService.save(existing);
        redirectAttributes.addFlashAttribute("success", "Cập nhật voucher thành công!");
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        promotionService.deleteById(id);
        redirectAttributes.addFlashAttribute("success", "Xóa voucher thành công!");
        return "redirect:/admin/vouchers";
    }
}
