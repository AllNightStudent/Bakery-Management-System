package com.swp.service;

import com.swp.entity.CartEntity;
import com.swp.entity.CartItemEntity;
import com.swp.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<CartItemEntity> findAllByCart(CartEntity cartEntity) {
        return cartItemRepository.findAll().stream().filter(ci -> ci.getCart().equals(cartEntity)).toList();
    }

    public void updateQuantity(Long id, int quantity) {
        CartItemEntity item = cartItemRepository.findById(id).orElseThrow();
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    public void deleteById(Long id) {
        cartItemRepository.deleteById(id);
    }


}