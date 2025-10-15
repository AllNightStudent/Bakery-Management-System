package com.swp.repository;

import com.swp.entity.CartEntity;
import com.swp.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository  extends JpaRepository<CartEntity, Long>{
    CartEntity findByUser(UserEntity user);
}
