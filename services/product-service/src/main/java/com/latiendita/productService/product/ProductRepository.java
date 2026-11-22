package com.latiendita.productService.product;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    /**
     * Shared predicates for the admin table and its CSV export. `status` is
     * derived from quantity + published, so it is filtered in JPQL rather than
     * mapped to a stored column.
     */
    String ADMIN_SEARCH_WHERE = """
            where (:q is null or (lower(p.title) like lower(concat('%', cast(:q as string), '%'))
                              or lower(p.sku) like lower(concat('%', cast(:q as string), '%'))))
              and (:categoryId is null or p.categoryId = :categoryId)
              and (:status is null
                   or (:status = 'agotado' and (p.quantity is null or p.quantity <= 0))
                   or (:status = 'publicado' and p.quantity > 0 and p.published = true)
                   or (:status = 'borrador' and p.quantity > 0
                       and (p.published = false or p.published is null)))
            """;

    String ADMIN_COUNT_WHERE = """
            where (:q is null or (lower(p.title) like lower(concat('%', cast(:q as string), '%'))
                              or lower(p.sku) like lower(concat('%', cast(:q as string), '%'))))
              and (:categoryId is null or p.categoryId = :categoryId)
            """;

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

    Page<ProductEntity> findByQuantityLessThanOrderByQuantityAsc(int threshold, Pageable pageable);

    long countByQuantityLessThan(int threshold);

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

    @Query("select p from ProductEntity p " + ADMIN_SEARCH_WHERE)
    Page<ProductEntity> searchAdmin(@Param("q") String q,
                                    @Param("status") String status,
                                    @Param("categoryId") Long categoryId,
                                    Pageable pageable);

    @Query("select p from ProductEntity p " + ADMIN_SEARCH_WHERE)
    List<ProductEntity> searchAdminList(@Param("q") String q,
                                        @Param("status") String status,
                                        @Param("categoryId") Long categoryId,
                                        Sort sort);

    @Query("select count(p) from ProductEntity p " + ADMIN_COUNT_WHERE)
    long countAdmin(@Param("q") String q, @Param("categoryId") Long categoryId);

    @Query("select count(p) from ProductEntity p " + ADMIN_COUNT_WHERE
            + " and p.quantity < :threshold")
    long countAdminLowStock(@Param("q") String q,
                            @Param("categoryId") Long categoryId,
                            @Param("threshold") int threshold);
}
