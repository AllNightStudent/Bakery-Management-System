package com.swp.service;

import com.swp.dto.request.CreateProductRequest;
import com.swp.entity.CategoryEntity;
import com.swp.entity.ProductEntity;
import com.swp.entity.ProductVariantEntity;
import com.swp.repository.ProductRepository;
import com.swp.repository.ProductVariantRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    public ProductService(ProductRepository productRepository, ProductVariantRepository productVariantRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
    }

    private static final Path UPLOAD_ROOT = Paths.get("C:/UploadImg");

    @Transactional
    public Long createProduct(CreateProductRequest request, CategoryEntity category) {
        // Validate duplicate product name
        if (productRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên sản phẩm đã tồn tại: " + request.getName());
        }

        // Validate duplicate SKUs
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (var variant : request.getVariants()) {
                if (variant.getSku() != null && !variant.getSku().trim().isEmpty()) {
                    if (productVariantRepository.existsBySku(variant.getSku().trim())) {
                        throw new RuntimeException("SKU đã tồn tại: " + variant.getSku());
                    }
                }
            }
        }

        String storedImagePath = storeImage(request.getImageFile());

        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .shortDescription(request.getShortDescription())
                .imgUrl(storedImagePath)
                .description(request.getDescription())
                .categoryId(category)
                .active(true)
                .build();
        product = productRepository.save(product);

        if (request.getVariants() != null) {
            for (var v : request.getVariants()) {
                ProductVariantEntity variant = ProductVariantEntity.builder()
                        .product(product)
                        .sku(v.getSku())
                        .weight(v.getWeight())
                        .price(v.getPrice())
                        .stock(v.getStock())
                        .expiryDate(v.getExpiryDate())
                        .build();
                productVariantRepository.save(variant);
            }
        }

        return product.getProductId();
    }

    public ProductEntity getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
    }

    public List<ProductVariantEntity> getProductVariants(Long productId) {
        // Verify product exists
        getProductById(productId);
        return productVariantRepository.findByProductProductId(productId);
    }


    @Transactional
    public void updateProduct(Long productId, CreateProductRequest request, CategoryEntity category) {
        ProductEntity product = getProductById(productId);

        // Validate product name
        if (!product.getName().equals(request.getName())
                && productRepository.existsByName(request.getName())) {
            throw new RuntimeException("Tên sản phẩm đã tồn tại: " + request.getName());
        }

        // Update product basic info
        product.setName(request.getName());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        product.setCategoryId(category);
        product.setActive(request.getActive());

        if (request.getImageFile() != null && !request.getImageFile().isEmpty() && request.getImageFile().getSize() > 0) {
            String newImagePath = storeImage(request.getImageFile());
            product.setImgUrl(newImagePath);
        }

        productRepository.save(product);

        // ================================
        //  VARIANT UPDATE WITH CORRECT VALIDATION
        // ================================

        List<ProductVariantEntity> existingVariants =
                productVariantRepository.findByProductProductId(productId);

        // Convert to map for quick lookup
        Map<String, ProductVariantEntity> existingMap = existingVariants.stream()
                .filter(v -> v.getSku() != null)
                .collect(Collectors.toMap(v -> v.getSku().trim(), v -> v));

        Set<String> requestSkus = new HashSet<>();

        if (request.getVariants() != null) {
            for (var variantReq : request.getVariants()) {

                String sku = variantReq.getSku() != null ? variantReq.getSku().trim() : null;
                if (sku == null || sku.isEmpty()) continue;

                requestSkus.add(sku);

                // CASE 1: SKU has existed in THIS product → update
                if (existingMap.containsKey(sku)) {

                    ProductVariantEntity old = existingMap.get(sku);

                    old.setWeight(variantReq.getWeight());
                    old.setPrice(variantReq.getPrice());
                    old.setStock(variantReq.getStock());
                    old.setExpiryDate(variantReq.getExpiryDate());

                    productVariantRepository.save(old);
                }

                // CASE 2: SKU not in this product → validate against other products
                else {

                    // ❗ Validate SKU exists but NOT in this product
                    if (productVariantRepository.existsBySkuAndProduct_ProductIdNot(sku, productId)) {
                        throw new RuntimeException("SKU đã tồn tại ở sản phẩm khác: " + sku);
                    }

                    // Create new variant
                    ProductVariantEntity newVariant = ProductVariantEntity.builder()
                            .product(product)
                            .sku(sku)
                            .weight(variantReq.getWeight())
                            .price(variantReq.getPrice())
                            .stock(variantReq.getStock())
                            .expiryDate(variantReq.getExpiryDate())
                            .build();

                    productVariantRepository.save(newVariant);
                }
            }
        }

        // CASE 3: remove variants that were not included in request
        for (ProductVariantEntity oldVariant : existingVariants) {
            String oldSku = oldVariant.getSku() != null ? oldVariant.getSku().trim() : null;

            if (oldSku != null && !requestSkus.contains(oldSku)) {
                productVariantRepository.delete(oldVariant);
            }
        }
    }


    @Transactional
    public void deleteProduct(Long productId) {
        ProductEntity product = getProductById(productId);
        product.setActive(false);
//
//        // Delete all variants first (cascade delete)
//        List<ProductVariantEntity> variants = productVariantRepository.findByProductProductId(productId);
//        productVariantRepository.deleteAll(variants);

        // Delete the product
//        productRepository.delete(product);
    }

    private String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "";
        }
        try {
            if (!Files.exists(UPLOAD_ROOT)) {
                Files.createDirectories(UPLOAD_ROOT);
            }
            String original = file.getOriginalFilename();
            String filename = System.currentTimeMillis() + "_" + (original == null ? "image" : original.replaceAll("\\s+", "_"));
            Path destination = UPLOAD_ROOT.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            // Return web-accessible path instead of absolute file path
            return "/images/" + filename;
        } catch (IOException e) {
            return "";
        }
    }

    public List<ProductEntity> getAllProducts() {
        return productRepository.findAllActiveWithVariants();
    }

    public ProductEntity getProductVariantsById(Long productId) {
        return productRepository.findAllWithVariantsById(productId);
    }

}