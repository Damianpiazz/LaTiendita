package com.latiendita.productService.product;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    Optional<ProductEntity> findBySku(String sku);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    Optional<ProductEntity> findBySlug(String slug);

    Page<ProductEntity> findByCategoryId(Long categoryId, Pageable pageable);

    long countByCategoryId(Long categoryId);

    Page<ProductEntity> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<ProductEntity> findByCategoryIdAndTitleContainingIgnoreCase(
            Long categoryId, String title, Pageable pageable);

    /**
     * Single catalog query with nullable filters. Keeping it in the repository
     * preserves pagination and lets the database apply the featured flag.
     */
    @Query("""
            select p from ProductEntity p
            where (:q is null or lower(p.title) like lower(concat('%', cast(:q as string), '%')))
              and (:categoryId is null or p.categoryId = :categoryId)
              and (:featured is null or p.featured = :featured)
            """)
    Page<ProductEntity> search(@Param("q") String q,
                               @Param("categoryId") Long categoryId,
                               @Param("featured") Boolean featured,
                               Pageable pageable);
}
