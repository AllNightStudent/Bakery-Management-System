package com.swp.service;


import com.swp.dto.CategoryDTO;
import com.swp.entity.CategoryEntity;
import com.swp.repository.CategoryRepository;
import com.swp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public Page<CategoryDTO> getAllCategories(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return categoryRepository.findAll(pageable).map(this::convertToDTO);
    }

    public Page<CategoryDTO> searchCategories(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return categoryRepository.searchCategories(keyword, pageable).map(this::convertToDTO);
    }

    public Page<CategoryDTO> getCategoriesByStatus(Boolean active, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return categoryRepository.findByActive(active, pageable).map(this::convertToDTO);
    }

    public List<CategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByActive(true, Pageable.unpaged())
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        return convertToDTO(category);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new RuntimeException("Tên danh mục đã tồn tại: " + categoryDTO.getName());
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .active(categoryDTO.getActive() != null ? categoryDTO.getActive() : true)
                .timeUnit(categoryDTO.getTimeUnit())
                .expireTime(categoryDTO.getExpireTime())
                .build();

        CategoryEntity savedCategory = categoryRepository.save(category);
        return convertToDTO(savedCategory);
    }

    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        if (categoryRepository.existsByNameAndIdNot(categoryDTO.getName(), id)) {
            throw new RuntimeException("Tên danh mục đã tồn tại: " + categoryDTO.getName());
        }

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setActive(categoryDTO.getActive());
        category.setTimeUnit(categoryDTO.getTimeUnit());
        category.setExpireTime(categoryDTO.getExpireTime());

        CategoryEntity updatedCategory = categoryRepository.save(category);
        return convertToDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));

        // Kiểm tra xem có Product nào thuộc category này không
        boolean hasProducts = productRepository.existsByCategoryId(category);
        if (hasProducts) {
            throw new RuntimeException("Không thể xóa danh mục vì còn sản phẩm thuộc danh mục này!");
        }

        // Nếu không có product nào, mới xóa
        categoryRepository.delete(category);
    }


    @Transactional
    public void toggleCategoryStatus(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        category.setActive(!category.getActive());
        categoryRepository.save(category);
    }

    private CategoryDTO convertToDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .expireTime(entity.getExpireTime())
                .timeUnit(entity.getTimeUnit())
                .build();
    }

    public List<CategoryEntity> getAllCategoriesActivated() {
        return categoryRepository.getAllActivated();
    }
}