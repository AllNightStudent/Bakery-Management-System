package com.swp.controller.product;

import com.swp.dto.CategoryDTO;
import com.swp.entity.*;
import com.swp.repository.CartItemRepository;
import com.swp.repository.ProductVariantRepository;
import com.swp.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserService userService;
    private final CartItemRepository cartItemRepository;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            Model model) {

        List<CategoryEntity> categories = categoryService.getAllCategoriesActivated();
        model.addAttribute("categories", categories);

        // Lấy tất cả sản phẩm
        List<ProductEntity> allProducts = productService.getAllProducts();

        // Lọc theo category
        if (categoryId != null) {
            allProducts = allProducts.stream()
                    .filter(p -> p.getCategoryId().getId().equals(categoryId))
                    .toList();
        }

        // Lọc theo search
        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.trim().toLowerCase();
            allProducts = allProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(keyword) ||
                            (p.getShortDescription() != null && p.getShortDescription().toLowerCase().contains(keyword)))
                    .toList();
        }

        // Tính tổng trang
        int totalProducts = allProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / size);

        // Giới hạn sản phẩm hiển thị theo trang
        int fromIndex = Math.max(0, (page - 1) * size);
        int toIndex = Math.min(fromIndex + size, totalProducts);
        List<ProductEntity> paginatedProducts = allProducts.subList(fromIndex, toIndex);

        model.addAttribute("products", paginatedProducts);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "products";
    }




    @GetMapping("/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model) {
        ProductEntity productDetail = productService.getProductVariantsById(id);
        Long catId = productDetail.getCategoryId().getId();
        List<ProductEntity> products = productService.getAllProducts().stream().filter(p ->
                        p.getCategoryId().getId().equals(catId)).limit(4)
                .toList();

        model.addAttribute("products", products);
        model.addAttribute("productDetail", productDetail);
        return "productDetail";
    }

    @GetMapping("/{id}/add/{variantId}")
    public String addProductToCart(@PathVariable("id") Long id,
                                   @PathParam("variantId") Long variantId,
                                   @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        UserEntity currentUser = userService.getCurrentUser();
        if(currentUser == null) {
            return "redirect:/login";
        }

        CartEntity cart = cartService.findCartByUser(currentUser);
        ProductVariantEntity productVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        Integer stock = productVariant.getStock();

        if(quantity > stock) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Số lượng trong kho không đủ, vui lòng chọn số lượng dưới " + stock);
            return "redirect:/products/" + id;
        }

        CartItemEntity existingItem = cartItemRepository.findByCartAndProductVariantId(cart, productVariant);
        if (existingItem != null) {
            Integer totalQuantity = existingItem.getQuantity() + quantity;
            if(totalQuantity > stock) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Bạn đã có " + existingItem.getQuantity() +
                                " sản phẩm trong giỏ. Tồn kho chỉ còn " + stock + ".");
                return "redirect:/products/" + id;
            }
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemService.save(existingItem);
        } else {
            CartItemEntity cartItem = new CartItemEntity();
            cartItem.setProductVariantId(productVariant);
            cartItem.setQuantity(quantity);
            cartItem.setCart(cart);
            cartItemService.save(cartItem);
        }
        return "redirect:/cart" ;
    }

    @PostMapping("/buy-now")
    public String buyNow(@RequestParam("variantId") Long variantId,
                         @RequestParam("quantity") int quantity,
                         HttpSession session) {
        UserEntity currentUser = userService.getCurrentUser();
        if(currentUser == null) {
            return "redirect:/login";
        }

        // Lưu thông tin mua ngay vào session
        session.setAttribute("buyNowVariantId", variantId);
        session.setAttribute("buyNowQuantity", quantity);

        // Chuyển thẳng sang trang checkout
        return "redirect:/order/checkout";
    }
}