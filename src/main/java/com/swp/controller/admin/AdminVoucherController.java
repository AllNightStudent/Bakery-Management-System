package com.swp.controller.admin;

import com.swp.entity.Promotion;
import com.swp.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    private final PromotionRepository promotionRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", promotionRepository.findAll());
        return "admin/voucher-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("voucher", new Promotion());
        return "admin/voucher-form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("voucher") Promotion voucher) {
        voucher.setActive(true);
        promotionRepository.save(voucher);
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Promotion voucher = promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));
        model.addAttribute("voucher", voucher);
        return "admin/voucher-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                         @ModelAttribute("voucher") Promotion form) {
        form.setId(id);
        promotionRepository.save(form);
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        promotionRepository.deleteById(id);
        return "redirect:/admin/vouchers";
    }
}
