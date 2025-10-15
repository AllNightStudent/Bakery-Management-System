package com.swp.service;

import com.swp.dto.request.CreateProductRequest;
import com.swp.entity.CategoryEntity;
import com.swp.entity.ProductEntity;
import com.swp.entity.ProductVariantEntity;
import com.swp.repository.ProductRepository;
import com.swp.repository.ProductVariantRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

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
            throw new IllegalArgumentException("Tên sản phẩm đã tồn tại: " + request.getName());
        }

        // Validate duplicate SKUs
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (var variant : request.getVariants()) {
                if (variant.getSku() != null && !variant.getSku().trim().isEmpty()) {
                    if (productVariantRepository.existsBySku(variant.getSku().trim())) {
                        throw new IllegalArgumentException("SKU đã tồn tại: " + variant.getSku());
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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));
    }

    public List<ProductVariantEntity> getProductVariants(Long productId) {
        // Verify product exists
        getProductById(productId);
        return productVariantRepository.findByProductProductId(productId);
    }


    @Transactional
    public void updateProduct(Long productId, CreateProductRequest request, CategoryEntity category) {
        ProductEntity product = getProductById(productId);

        // Validate duplicate product name (exclude current product)
        if (!product.getName().equals(request.getName()) && productRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Tên sản phẩm đã tồn tại: " + request.getName());
        }

        // Update product basic info
        product.setName(request.getName());
        product.setShortDescription(request.getShortDescription());
        product.setDescription(request.getDescription());
        product.setCategoryId(category);

        // Handle image update
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String newImagePath = storeImage(request.getImageFile());
            product.setImgUrl(newImagePath);
        }

        productRepository.save(product);

        // Delete existing variants FIRST
        List<ProductVariantEntity> existingVariants = productVariantRepository.findByProductProductId(productId);
        productVariantRepository.deleteAll(existingVariants);

        // Validate duplicate SKUs against ALL other SKUs in database
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            for (var variant : request.getVariants()) {
                if (variant.getSku() != null && !variant.getSku().trim().isEmpty()) {
                    String sku = variant.getSku().trim();

                    // Check if SKU exists in database (compare with ALL SKUs)
                    if (productVariantRepository.existsBySku(sku)) {
                        throw new IllegalArgumentException("SKU đã tồn tại: " + sku);
                    }
                }
            }
        }

        // Add new variants
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
    }

    @Transactional
    public void deleteProduct(Long productId) {
        ProductEntity product = getProductById(productId);

        // Delete all variants first (cascade delete)
        List<ProductVariantEntity> variants = productVariantRepository.findByProductProductId(productId);
        productVariantRepository.deleteAll(variants);

        // Delete the product
        productRepository.delete(product);
    }

    private String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
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
            throw new RuntimeException("Lưu ảnh thất bại", e);
        }
    }

    public List<ProductEntity> getAllProducts() {
        return productRepository.findAllWithVariants();
    }

    public ProductEntity getProductVariantsById(Long productId) {
        return productRepository.findAllWithVariantsById(productId);
    }

}


