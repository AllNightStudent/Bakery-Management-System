package com.swp.migrate;

import com.swp.entity.ProductEntity;
import com.swp.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class ProductInit  implements CommandLineRunner {
    private final ProductRepository productRepository;


    @Override
    public void run(String... args) throws Exception {
        List<ProductEntity> products = productRepository.findByActiveIsNull();
        if (!products.isEmpty()) {
            for (ProductEntity product : products) {
                product.setActive(Boolean.TRUE);
            }
            productRepository.saveAll(products);
        }
    }
}