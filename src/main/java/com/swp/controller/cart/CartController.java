package com.swp.controller.cart;

import com.swp.entity.CartEntity;
import com.swp.entity.CartItemEntity;
import com.swp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final CartItemService cartItemService;
    private final UserService userService;

    @GetMapping
    public String viewCartItem(Model model) {
        CartEntity cart = cartService.findCartByUser(userService.getCurrentUser());
        List<CartItemEntity> items = cartItemService.findAllByCart(cart);


        model.addAttribute("cartItems", items);
        return "cart";
    }

    @PostMapping("/update")
    public String updateCartItems(
            @RequestParam("id") List<Long> ids,
            @RequestParam("quantity") List<Integer> quantities
    ) {
        for (int i = 0; i < ids.size(); i++) {
            cartItemService.updateQuantity(ids.get(i), quantities.get(i));
        }
        return "redirect:/cart";
    }



}
