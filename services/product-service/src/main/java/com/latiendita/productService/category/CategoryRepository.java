package com.latiendita.productService.category;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    Optional<CategoryEntity> findByName(String name);

    Page<CategoryEntity> findByFeatured(boolean featured, Pageable pageable);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    /** Case-insensitive substring on `name`, combinable with the Home flag. */
    @Query("""
            select c from CategoryEntity c
            where (:q is null or lower(c.name) like lower(concat('%', cast(:q as string), '%')))
              and (:featured is null or c.featured = :featured)
            """)
    Page<CategoryEntity> search(@Param("q") String q,
                               @Param("featured") Boolean featured,
                               Pageable pageable);

    /**
     * Products under the same threshold used by `/api/products/admin/low-stock`:
     * `quantity < threshold`. Referencing ProductEntity here keeps the product
     * repository untouched while reusing the single source of truth for counts.
     */
    @Query("""
            select count(p) from ProductEntity p
            where p.categoryId = :categoryId and p.quantity < :threshold
            """)
    long countLowStockByCategoryId(@Param("categoryId") Long categoryId,
                                   @Param("threshold") int threshold);
}
