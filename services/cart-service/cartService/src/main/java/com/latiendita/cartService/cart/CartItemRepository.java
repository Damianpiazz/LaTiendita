package com.latiendita.cartService.cart;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {

    List<CartItemEntity> findAllByProductId(Long productId);
}
