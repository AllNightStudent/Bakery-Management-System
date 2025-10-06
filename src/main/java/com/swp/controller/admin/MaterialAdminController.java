package com.swp.controller.admin;

import com.swp.dto.MaterialDTO;
import com.swp.service.MaterialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/materials")
@RequiredArgsConstructor
public class MaterialAdminController {

	private final MaterialService materialService;

	@GetMapping
	public String list(
			Model model,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "name") String sortBy,
			@RequestParam(defaultValue = "asc") String sortDir,
			@RequestParam(required = false) String keyword
	) {
		Page<MaterialDTO> materialPage = materialService.getMaterials(keyword, page, size, sortBy, sortDir);
		model.addAttribute("materialPage", materialPage);
		model.addAttribute("currentPage", page);
		model.addAttribute("size", size);
		model.addAttribute("totalPages", materialPage.getTotalPages());
		model.addAttribute("totalItems", materialPage.getTotalElements());
		model.addAttribute("keyword", keyword);
		model.addAttribute("sortBy", sortBy);
		model.addAttribute("sortDir", sortDir);
		model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
		model.addAttribute("materialForm", new MaterialDTO());
		return "admin/materials";
	}

	@PostMapping("/create")
	public String create(
			@Valid @ModelAttribute("materialForm") MaterialDTO dto,
			BindingResult result,
			RedirectAttributes redirectAttributes
	) {
		if (result.hasErrors()) {
			String msg = result.hasFieldErrors() && result.getFieldError() != null
					? result.getFieldError().getDefaultMessage()
					: "Dữ liệu không hợp lệ";
			redirectAttributes.addFlashAttribute("error", msg);
			return "redirect:/admin/materials";
		}
		try {
			materialService.create(dto);
			redirectAttributes.addFlashAttribute("success", "Thêm nguyên liệu thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/materials";
	}

	@PostMapping("/update/{id}")
	public String update(
			@PathVariable Long id,
			@Valid @ModelAttribute("materialForm") MaterialDTO dto,
			BindingResult result,
			RedirectAttributes redirectAttributes
	) {
		if (result.hasErrors()) {
			String msg = result.hasFieldErrors() && result.getFieldError() != null
					? result.getFieldError().getDefaultMessage()
					: "Dữ liệu không hợp lệ";
			redirectAttributes.addFlashAttribute("error", msg);
			return "redirect:/admin/materials";
		}
		try {
			materialService.update(id, dto);
			redirectAttributes.addFlashAttribute("success", "Cập nhật nguyên liệu thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/materials";
	}

	@PostMapping("/delete/{id}")
	public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			materialService.delete(id);
			redirectAttributes.addFlashAttribute("success", "Xóa nguyên liệu thành công!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
		}
		return "redirect:/admin/materials";
	}
}


