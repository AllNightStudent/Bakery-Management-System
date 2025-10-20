package com.swp.controller.admin;

import com.swp.dto.request.CreateProductRequest;
import com.swp.entity.CategoryEntity;
import com.swp.entity.enums.TimeUnit;
import com.swp.repository.CategoryRepository;
import com.swp.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.swp.entity.ProductEntity;
import com.swp.entity.ProductVariantEntity;
import com.swp.repository.ProductRepository;
import com.swp.repository.ProductVariantRepository;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;


    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("createProductRequest") CreateProductRequest request,
                         BindingResult bindingResult,
                         Model model,
                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                model.addAttribute("categories", categoryRepository.findAll());
                return "admin/products";
            }
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
            TimeUnit timeUnit = category.getTimeUnit();
            Integer expireTime = category.getExpireTime();
            request.getVariants().forEach(product -> {
                LocalDate expiryDate = null;
                if (expireTime != null && expireTime > 0 && timeUnit != null) {
                    LocalDate now = LocalDate.now();
                    switch (timeUnit) {
                        case DAY -> expiryDate = now.plusDays(expireTime);
                        case WEEK -> expiryDate = now.plusWeeks(expireTime);
                        case MONTH -> expiryDate = now.plusMonths(expireTime);
                        case YEAR -> expiryDate = now.plusYears(expireTime);
                    }
                }
                product.setExpiryDate(expiryDate);
            });
            productService.createProduct(request, category);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo sản phẩm thành công");
            return "redirect:/admin/products";

        } catch (IllegalArgumentException e) {
            // Handle validation errors (duplicate name/SKU)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        } catch (Exception e) {
            // Handle other unexpected errors
            System.err.println("Error creating product: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi tạo sản phẩm: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getProduct(@PathVariable Long id) {
        try {
            var product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/variants")
    @ResponseBody
    public ResponseEntity<?> getProductVariants(@PathVariable Long id) {
        try {
            var variants = productService.getProductVariants(id);
            return ResponseEntity.ok(variants);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra: " + e.getMessage());
        }
    }


    @PostMapping("/{id}/update")
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("createProductRequest") CreateProductRequest request,
                                BindingResult bindingResult,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ");
                return "redirect:/admin/products";
            }
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
            TimeUnit timeUnit = category.getTimeUnit();
            Integer expireTime = category.getExpireTime();
            request.getVariants().forEach(product -> {
                LocalDate expiryDate = null;
                if (expireTime != null && expireTime > 0 && timeUnit != null) {
                    LocalDate now = LocalDate.now();
                    switch (timeUnit) {
                        case DAY -> expiryDate = now.plusDays(expireTime);
                        case WEEK -> expiryDate = now.plusWeeks(expireTime);
                        case MONTH -> expiryDate = now.plusMonths(expireTime);
                        case YEAR -> expiryDate = now.plusYears(expireTime);
                    }
                }
                product.setExpiryDate(expiryDate);
            });
            productService.updateProduct(id, request, category);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công");
            return "redirect:/admin/products";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        } catch (Exception e) {
            System.err.println("Error updating product: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi cập nhật sản phẩm: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id,
                                org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công");
            return "redirect:/admin/products";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/products";
        } catch (Exception e) {
            System.err.println("Error deleting product: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa sản phẩm: " + e.getMessage());
            return "redirect:/admin/products";
        }
    }

    @GetMapping("")
    public String products(Model model,
                           @RequestParam(name = "page", defaultValue = "0") int page,
                           @RequestParam(name = "size", defaultValue = "10") int size,
                           @RequestParam(name = "category", defaultValue = "") String category,
                           @RequestParam(name = "search", defaultValue = "") String search) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        // Determine which search method to use
        Page<ProductEntity> productPage;
        if (!category.isEmpty() && !search.isEmpty()) {
            productPage = productRepository.findByCategoryAndName(category, search, pageable);
        } else if (!category.isEmpty()) {
            productPage = productRepository.findByCategoryNameContaining(category, pageable);
        } else if (!search.isEmpty()) {
            productPage = productRepository.findByNameOrShortDescriptionContaining(search, pageable);

            if (productPage.getContent().isEmpty()) {
                List<ProductVariantEntity> variantsBySku = productVariantRepository.findBySkuContaining(search);
                if (!variantsBySku.isEmpty()) {
                    // Get product IDs from variants
                    List<Long> productIds = variantsBySku.stream()
                            .map(variant -> variant.getProduct().getProductId())
                            .distinct()
                            .collect(Collectors.toList());

                    // Get products by IDs - create a custom query for this
                    productPage = productRepository.findProductsByIds(productIds, pageable);
                }
            }
        } else {
            // No search criteria, show all products
            productPage = productRepository.findAll(pageable);
        }

        // Load variants for all products in the current page
        List<Long> productIds = productPage.getContent().stream()
                .map(ProductEntity::getProductId)
                .collect(Collectors.toList());

        List<ProductVariantEntity> allVariants = productVariantRepository.findAll().stream()
                .filter(variant -> productIds.contains(variant.getProduct().getProductId()))
                .collect(Collectors.toList());

        // Group variants by product ID
        Map<Long, List<ProductVariantEntity>> variantsByProduct = allVariants.stream()
                .collect(Collectors.groupingBy(variant -> variant.getProduct().getProductId()));

        model.addAttribute("createProductRequest", new CreateProductRequest());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("productPage", productPage);
        model.addAttribute("variantsByProduct", variantsByProduct);
        return "admin/products";
    }
}