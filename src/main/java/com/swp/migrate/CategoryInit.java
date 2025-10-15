package com.swp.migrate;

import com.swp.entity.CategoryEntity;
import com.swp.entity.enums.TimeUnit;
import com.swp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class CategoryInit implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(CategoryEntity.builder()
                    .name("Bánh Sinh Nhật")
                    .description("Các loại bánh sinh nhật phong phú, kem tươi, kem bơ, mousse.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Bánh Trung Thu")
                    .description("Bánh trung thu truyền thống và hiện đại, đa dạng hương vị.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Bánh Mỳ & Bánh Mặn")
                    .description("Bánh mỳ, bánh ngọt mặn và các sản phẩm tiện lợi khác.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Cookies & Mini Cake")
                    .description("Cookies, bánh mini, petit và các loại bánh nhỏ khác.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Kem & Caramen")
                    .description("Kem, caramen và các món tráng miệng lạnh.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Bánh Đặc Biệt & Tạo Hình")
                    .description("Bánh vẽ, bánh 3D, bánh in ảnh, bánh số, chữ và tầng.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Bánh Tart & Pie")
                    .description("Bánh tart trái cây, bánh pie ngọt và mặn.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Bánh Âu")
                    .description("Bánh Pháp, bánh ngọt Âu, croissant, brioche và puff pastry.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Bánh Nhật & Hàn Quốc")
                    .description("Bánh kiểu Nhật Bản và Hàn Quốc, mochi, matcha, bingsu mini.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Bánh Giáng Sinh & Lễ Hội")
                    .description("Bánh theo mùa, bánh Giáng Sinh, bánh Halloween, bánh Valentine.")
                    .active(true)
                    .build());

            categoryRepository.save(CategoryEntity.builder()
                    .name("Quà Tặng & Set Bánh")
                    .description("Set bánh quà tặng, hộp bánh dành cho dịp lễ, sinh nhật, tri ân khách hàng.")
                    .active(true)
                    .build());
        }

        List<CategoryEntity> categories = categoryRepository.findAll();
        for (CategoryEntity category : categories) {
            if (category.getTimeUnit() == null) {
                category.setExpireTime(1);          // gán mặc định
                category.setTimeUnit(TimeUnit.MONTH);         // hoặc TimeUnit.DAY nếu muốn có default
                categoryRepository.save(category);
            }
        }
    }
}

