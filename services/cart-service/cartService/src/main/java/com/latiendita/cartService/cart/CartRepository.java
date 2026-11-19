package com.latiendita.cartService.cart;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {

    Optional<CartEntity> findByUserId(String userId);

    @Query("select c from CartEntity c left join fetch c.items where c.id = :id")
    Optional<CartEntity> findWithItemsById(@Param("id") UUID id);

    @Query("select c from CartEntity c left join fetch c.items where c.userId = :userId")
    Optional<CartEntity> findWithItemsByUserId(@Param("userId") String userId);
}
