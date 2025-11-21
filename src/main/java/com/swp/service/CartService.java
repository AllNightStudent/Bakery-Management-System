package com.swp.service;

import com.swp.entity.CartEntity;
import com.swp.entity.CartItemEntity;
import com.swp.entity.UserEntity;
import com.swp.repository.CartItemRepository;
import com.swp.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserService userService;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

//    public CartService (UserService userService,  CartRepository cartRepository) {
//        this.userService = userService;
//        this.cartRepository = cartRepository;
//    }

    @Transactional
    public CartEntity findCartByUser(UserEntity user) {
        UserEntity currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        CartEntity cart = cartRepository.findByUser(currentUser);
        if (cart == null) {
            cart = new CartEntity();
            cart.setUser(currentUser);
            cartRepository.save(cart);
            return cart;
        }
        return cart;
    }

    public void updateCartItemQuantity(Long cartItemId, Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        CartItemEntity cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng"));
        int maxStock = cartItem.getProductVariantId().getStock();
        if (newQuantity > maxStock) {
            throw new IllegalArgumentException("Số lượng vượt quá hàng có sẵn. Tối đa: " + maxStock);
        }
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
    }
}