package com.swp.controller.product;

import com.swp.dto.CategoryDTO;
import com.swp.entity.*;
import com.swp.service.*;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserService userService;

    @GetMapping
    public String listProducts(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<CategoryEntity> categories = categoryService.getAllCategoriesActivated();
        model.addAttribute("categories", categories);

        List<ProductEntity> listOfProducts = productService.getAllProducts();

        // lọc theo category
        if (categoryId != null) {
            listOfProducts = listOfProducts.stream()
                    .filter(p -> p.getCategoryId().getId().equals(categoryId))
                    .toList();
        }

        // lọc theo search
        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.trim().toLowerCase();
            listOfProducts = listOfProducts.stream()
                    .filter(p -> p.getName().toLowerCase().contains(keyword) ||
                            (p.getShortDescription() != null && p.getShortDescription().toLowerCase().contains(keyword)))
                    .toList();
        }

        model.addAttribute("products", listOfProducts);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("search", search);

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
                                   Model model) {
        UserEntity currentUser = userService.getCurrentUser();
        if(currentUser == null) {
            return "redirect:/login";
        }

        CartEntity cart = cartService.findCartByUser(currentUser);

        ProductEntity productDetail = productService.getProductVariantsById(id);
        ProductVariantEntity productVariant = productDetail.getVariants().stream().filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .orElse(null);

        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setProductVariantId(productVariant);
        cartItem.setQuantity(quantity);
        cartItem.setCart(cart);
        cartItemService.save(cartItem);

        return "redirect:/products/" + id;


    }
}