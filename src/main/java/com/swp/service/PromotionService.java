package com.swp.service;

import com.swp.dto.PromotionResult;
import com.swp.dto.VoucherView;
import com.swp.entity.*;
import com.swp.entity.enums.DiscountType;
import com.swp.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;


    public List<Promotion> findAll() {
        return promotionRepository.findAll();
    }

    public Promotion findByIdOrThrow(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voucher không tồn tại"));
    }

    public Promotion save(Promotion promotion) {
        syncActiveWithDates(promotion);
        return promotionRepository.save(promotion);
    }

    public void deleteById(Long id) {
        promotionRepository.deleteById(id);
    }


    private void syncActiveWithDates(Promotion p) {
        LocalDateTime now = LocalDateTime.now();
        boolean isActive = true;

        if (p.getStartDate() != null && p.getStartDate().isAfter(now)) {
            isActive = false;
        }

        if (p.getEndDate() != null && p.getEndDate().isBefore(now)) {
            isActive = false;
        }

        p.setActive(isActive);
    }


    public PromotionResult applyBestPromotion(List<CartItemEntity> cartItems) {
        BigDecimal subtotal = calcSubtotal(cartItems);
        int totalQuantity = calcTotalQuantity(cartItems);

        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return new PromotionResult(null, subtotal, BigDecimal.ZERO, subtotal);
        }

        List<Promotion> promotions = promotionRepository.findAllActive(LocalDateTime.now());

        Promotion best = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;

        for (Promotion p : promotions) {
            if (!isEligible(p, subtotal, totalQuantity)) continue;

            BigDecimal discount = calculateDiscount(p, subtotal);
            if (discount.compareTo(bestDiscount) > 0) {
                bestDiscount = discount;
                best = p;
            }
        }

        BigDecimal finalTotal = subtotal.subtract(bestDiscount);
        if (finalTotal.compareTo(BigDecimal.ZERO) < 0) {
            finalTotal = BigDecimal.ZERO;
        }

        return new PromotionResult(best, subtotal, bestDiscount, finalTotal);
    }

    private BigDecimal calcSubtotal(List<CartItemEntity> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemEntity item : items) {
            ProductVariantEntity variant = item.getProductVariantId();
            if (variant == null || variant.getPrice() == null) {
                continue;
            }
            BigDecimal unitPrice = variant.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }
        return subtotal;
    }

    private int calcTotalQuantity(List<CartItemEntity> items) {
        int total = 0;
        for (CartItemEntity item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    private Set<CategoryEntity> extractCategories(List<CartItemEntity> items) {
        Set<CategoryEntity> result = new HashSet<>();
        for (CartItemEntity item : items) {
            ProductVariantEntity variant = item.getProductVariantId();
            if (variant == null) continue;

            ProductEntity product = variant.getProduct();
            if (product == null) continue;

            CategoryEntity category = product.getCategoryId();
            if (category != null) {
                result.add(category);
            }
        }
        return result;
    }

    private boolean isEligible(Promotion p,
                               BigDecimal subtotal,
                               int totalQuantity) {

        if (p.getMinOrderValue() != null &&
                subtotal.compareTo(p.getMinOrderValue()) < 0) {
            return false;
        }

        if (p.getMaxOrderValue() != null &&
                subtotal.compareTo(p.getMaxOrderValue()) > 0) {
            return false;
        }

        if (p.getMinTotalQuantity() != null &&
                totalQuantity < p.getMinTotalQuantity()) {
            return false;
        }



        return true;
    }

    private BigDecimal calculateDiscount(Promotion p, BigDecimal subtotal) {
        if (p.getDiscountType() == DiscountType.PERCENT) {
            if (p.getDiscountValue() == null) return BigDecimal.ZERO;
            BigDecimal percent = p.getDiscountValue();
            BigDecimal discount = subtotal
                    .multiply(percent)
                    .divide(BigDecimal.valueOf(100));
            return discount;
        } else if (p.getDiscountType() == DiscountType.AMOUNT) {
            if (p.getDiscountValue() == null) return BigDecimal.ZERO;
            return p.getDiscountValue().min(subtotal);
        }
        return BigDecimal.ZERO;
    }

    public List<VoucherView> getVoucherViews(List<CartItemEntity> cartItems) {
        BigDecimal subtotal = calcSubtotal(cartItems);
        int totalQuantity = calcTotalQuantity(cartItems);

        List<Promotion> promotions = promotionRepository.findAllActive(LocalDateTime.now());

        List<VoucherView> result = new ArrayList<>();
        for (Promotion p : promotions) {
            boolean ok = isEligible(p, subtotal, totalQuantity);
            BigDecimal discount = BigDecimal.ZERO;
            if (ok) {
                discount = calculateDiscount(p, subtotal);
            }
            result.add(new VoucherView(p, ok, discount));
        }
        return result;
    }

    public BigDecimal calculateDiscountForItems(Promotion promotion,
                                                List<CartItemEntity> cartItems) {
        if (promotion == null || cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = calcSubtotal(cartItems);
        int totalQuantity = calcTotalQuantity(cartItems);

        if (!isEligible(promotion, subtotal, totalQuantity)) {
            return BigDecimal.ZERO;
        }

        return calculateDiscount(promotion, subtotal);
    }

}
