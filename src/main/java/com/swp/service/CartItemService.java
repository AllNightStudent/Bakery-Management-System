package com.swp.service;

import com.swp.entity.CartItemEntity;
import com.swp.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemService {

    private final CartItemRepository cartItemRepository;

//    public CartItemService(CartItemRepository cartItemRepository) {
//        this.cartItemRepository = cartItemRepository;
//    }

    public void save(CartItemEntity cartItem) {
        cartItemRepository.save(cartItem);
    }

}