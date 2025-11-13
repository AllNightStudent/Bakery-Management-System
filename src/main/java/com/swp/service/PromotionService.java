package com.swp.service;

import com.swp.dto.PromotionResult;
import com.swp.dto.VoucherView;
import com.swp.entity.*;
import com.swp.entity.enums.DiscountType;
import com.swp.entity.enums.ProductScopeType;
import com.swp.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionResult applyBestPromotion(List<CartItemEntity> cartItems) {
        BigDecimal subtotal = calcSubtotal(cartItems);
        int totalQuantity = calcTotalQuantity(cartItems);
        Set<CategoryEntity> categoriesInCart = extractCategories(cartItems);

        if (subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return new PromotionResult(null, subtotal, BigDecimal.ZERO, subtotal);
        }

        List<Promotion> promotions = promotionRepository.findAllActive(LocalDateTime.now());

        Promotion best = null;
        BigDecimal bestDiscount = BigDecimal.ZERO;

        for (Promotion p : promotions) {
            if (!isEligible(p, subtotal, totalQuantity, categoriesInCart)) continue;

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

    // ================== PHẦN CHỈNH CHO CARTITEMENTITY ==================

    /** Tính tổng tiền hàng (subtotal) = sum(priceVariant * quantity) */
    private BigDecimal calcSubtotal(List<CartItemEntity> items) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItemEntity item : items) {
            ProductVariantEntity variant = item.getProductVariantId();
            if (variant == null || variant.getPrice() == null) {
                continue; // hoặc throw nếu bạn muốn bắt buộc phải có giá
            }
            BigDecimal unitPrice = variant.getPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }
        return subtotal;
    }

    /** Tổng số lượng sản phẩm trong giỏ */
    private int calcTotalQuantity(List<CartItemEntity> items) {
        int total = 0;
        for (CartItemEntity item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    /** Lấy tất cả Category xuất hiện trong giỏ hàng */
    private Set<CategoryEntity> extractCategories(List<CartItemEntity> items) {
        Set<CategoryEntity> result = new HashSet<>();
        for (CartItemEntity item : items) {
            ProductVariantEntity variant = item.getProductVariantId();
            if (variant == null) continue;

            // Lấy product từ variant
            ProductEntity product = variant.getProduct();   // CHÚ Ý: field này trong ProductVariantEntity
            if (product == null) continue;

            // Vì field trong ProductEntity là `categoryId`
            CategoryEntity category = product.getCategoryId();  // ✅ DÙNG getCategoryId(), KHÔNG phải getCategory()
            if (category != null) {
                result.add(category);
            }
        }
        return result;
    }

    // ================== PHẦN DƯỚI GIỮ NGUYÊN NHƯ CŨ ==================

    private boolean isEligible(Promotion p,
                               BigDecimal subtotal,
                               int totalQuantity,
                               Set<CategoryEntity> cartCategories) {

        // 1. Giá trị đơn hàng
        if (p.getMinOrderValue() != null &&
                subtotal.compareTo(p.getMinOrderValue()) < 0) {
            return false;
        }

        if (p.getMaxOrderValue() != null &&
                subtotal.compareTo(p.getMaxOrderValue()) > 0) {
            return false;
        }

        // 2. Số lượng
        if (p.getMinTotalQuantity() != null &&
                totalQuantity < p.getMinTotalQuantity()) {
            return false;
        }

        // 3. Loại sản phẩm
        ProductScopeType scope = p.getProductScopeType();
        if (scope == null || scope == ProductScopeType.ALL) {
            return true;
        }

        if (scope == ProductScopeType.CATEGORY) {
            if (p.getCategories() == null || p.getCategories().isEmpty()) {
                return false;
            }
            for (CategoryEntity c : cartCategories) {
                if (p.getCategories().contains(c)) {
                    return true;
                }
            }
            return false;
        }

        if (scope == ProductScopeType.PRODUCT) {
            // TODO: nếu sau này bạn làm khuyến mãi theo từng product cụ thể
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
        Set<CategoryEntity> categoriesInCart = extractCategories(cartItems);

        List<Promotion> promotions = promotionRepository.findAllActive(LocalDateTime.now());

        List<VoucherView> result = new ArrayList<>();
        for (Promotion p : promotions) {
            boolean ok = isEligible(p, subtotal, totalQuantity, categoriesInCart);
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

        BigDecimal subtotal = calcSubtotal(cartItems);             // dùng hàm private đã có
        int totalQuantity = calcTotalQuantity(cartItems);
        Set<CategoryEntity> categoriesInCart = extractCategories(cartItems);

        if (!isEligible(promotion, subtotal, totalQuantity, categoriesInCart)) {
            return BigDecimal.ZERO;
        }

        return calculateDiscount(promotion, subtotal);             // cũng là hàm private đã có
    }

}
