package com.latiendita.productService.product;

import com.latiendita.productService.category.CategoryEntity;
import com.latiendita.productService.category.CategoryRepository;
import com.latiendita.productService.exception.CategoryNotFoundException;
import com.latiendita.productService.exception.DuplicateSkuException;
import com.latiendita.productService.exception.DuplicateSlugException;
import com.latiendita.productService.exception.ProductNotFoundException;
import com.latiendita.productService.product.dto.AdminLowStockProductResponse;
import com.latiendita.productService.product.dto.AdminLowStockResponse;
import com.latiendita.productService.product.dto.AdminProductCsvWriter;
import com.latiendita.productService.product.dto.AdminProductMapper;
import com.latiendita.productService.product.dto.AdminProductResponse;
import com.latiendita.productService.product.dto.AdminProductSummaryResponse;
import com.latiendita.productService.product.dto.ProductDto;
import com.latiendita.productService.product.dto.ProductMapper;
import com.latiendita.productService.sort.SortValidator;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    public static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Propiedades de {@link ProductEntity} que se pueden usar en `sort`. Cualquier
     * otra (o un formato como `-id`) se rechaza con 400 antes de llegar a JPA.
     */
    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
            "id", "title", "imageUrl", "sku", "price", "quantity", "categoryId", "slug",
            "subcategory", "featured", "published", "originalPrice", "fulfillment");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductDto create(ProductDto dto) {
        if (productRepository.existsBySku(dto.sku())) {
            throw new DuplicateSkuException(dto.sku());
        }
        CategoryEntity category = validateCategory(dto.categoryId());

        String slug = resolveSlug(dto.slug(), dto.title(), dto.sku(), null);
        if (productRepository.existsBySlug(slug)) {
            throw new DuplicateSlugException(slug);
        }

        ProductEntity saved = productRepository.save(ProductEntity.builder()
                .title(dto.title())
                .imageUrl(dto.imageUrl())
                .sku(dto.sku())
                .price(dto.price())
                .quantity(dto.quantity())
                .categoryId(dto.categoryId())
                .slug(slug)
                .subcategory(dto.subcategory())
                .featured(dto.featured())
                .published(resolvePublished(dto.published()))
                .originalPrice(dto.originalPrice())
                .fulfillment(dto.fulfillment())
                .images(orEmpty(dto.images()))
                .description(orEmpty(dto.description()))
                .highlights(orEmpty(dto.highlights()))
                .specs(orEmpty(dto.specs()))
                .build());

        return ProductMapper.toDto(saved, category == null ? null : category.getName());
    }

    public ProductDto update(Long id, ProductDto dto) {
        ProductEntity product = findEntity(id);

        boolean skuChanged = !product.getSku().equals(dto.sku());
        if (skuChanged && productRepository.existsBySkuAndIdNot(dto.sku(), id)) {
            throw new DuplicateSkuException(dto.sku());
        }
        CategoryEntity category = validateCategory(dto.categoryId());

        String slug = resolveSlug(dto.slug(), dto.title(), dto.sku(), product.getSlug());
        if (!slug.equals(product.getSlug())
                && productRepository.existsBySlugAndIdNot(slug, id)) {
            throw new DuplicateSlugException(slug);
        }

        product.setTitle(dto.title());
        product.setImageUrl(dto.imageUrl());
        product.setSku(dto.sku());
        product.setPrice(dto.price());
        product.setQuantity(dto.quantity());
        product.setCategoryId(dto.categoryId());
        product.setSlug(slug);
        product.setSubcategory(dto.subcategory());
        product.setFeatured(dto.featured());
        product.setPublished(resolvePublished(dto.published()));
        product.setOriginalPrice(dto.originalPrice());
        product.setFulfillment(dto.fulfillment());
        product.setImages(orEmpty(dto.images()));
        product.setDescription(orEmpty(dto.description()));
        product.setHighlights(orEmpty(dto.highlights()));
        product.setSpecs(orEmpty(dto.specs()));

        ProductEntity saved = productRepository.save(product);

        return ProductMapper.toDto(saved, category == null ? null : category.getName());
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        SortValidator.validate(pageable.getSort(), SORTABLE_PROPERTIES);
        return toDtos(productRepository.findAll(pageable));
    }

    /**
     * Listado con filtros opcionales del catálogo, incluido `featured`.
     */
    @Transactional(readOnly = true)
    public Page<ProductDto> search(String q, Long categoryId, Pageable pageable) {
        return search(q, categoryId, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> search(String q, Long categoryId, Boolean featured,
                                   Pageable pageable) {
        SortValidator.validate(pageable.getSort(), SORTABLE_PROPERTIES);
        return toDtos(productRepository.search(normalize(q), categoryId, featured, pageable));
    }

    @Transactional(readOnly = true)
    public AdminLowStockResponse adminLowStock(int threshold, int size) {
        int safeThreshold = Math.max(threshold, 0);
        int safeSize = Math.max(size, 1);
        Page<ProductEntity> page = productRepository.findByQuantityLessThanOrderByQuantityAsc(
                safeThreshold, org.springframework.data.domain.PageRequest.of(0, safeSize));
        long total = productRepository.countByQuantityLessThan(safeThreshold);
        List<AdminLowStockProductResponse> items = page.getContent().stream()
                .map(product -> new AdminLowStockProductResponse(
                        product.getTitle(), product.getSku(), product.getQuantity()))
                .toList();
        return new AdminLowStockResponse(items, total, safeThreshold);
    }

    @Transactional(readOnly = true)
    public Page<AdminProductResponse> adminProducts(String q, String status, Long categoryId,
                                                    Pageable pageable) {
        SortValidator.validate(pageable.getSort(), SORTABLE_PROPERTIES);
        Page<ProductEntity> page = productRepository.searchAdmin(
                normalize(q), normalizeStatus(status), categoryId, pageable);
        Map<Long, String> names = categoryNames(page.getContent());
        return page.map(entity -> AdminProductMapper.toDto(
                entity, names.get(entity.getCategoryId())));
    }

    @Transactional(readOnly = true)
    public AdminProductSummaryResponse adminSummary(String q, Long categoryId) {
        String term = normalize(q);
        return new AdminProductSummaryResponse(
                productRepository.countAdmin(term, categoryId),
                categoryRepository.count(),
                productRepository.countAdminLowStock(term, categoryId, LOW_STOCK_THRESHOLD),
                LOW_STOCK_THRESHOLD);
    }

    @Transactional
    public void updatePublishedStatus(List<Long> productIds, boolean published) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        List<ProductEntity> products = productRepository.findAllById(productIds);
        products.forEach(product -> product.setPublished(published));
        productRepository.saveAll(products);
    }

    @Transactional
    public void deleteProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return;
        }
        productRepository.deleteAllById(productIds);
    }

    @Transactional(readOnly = true)
    public String exportAdminCsv(String q, String status, Long categoryId, Sort sort) {
        SortValidator.validate(sort, SORTABLE_PROPERTIES);
        List<ProductEntity> products = productRepository.searchAdminList(
                normalize(q), normalizeStatus(status), categoryId, sort);
        Map<Long, String> names = categoryNames(products);
        List<AdminProductResponse> rows = products.stream()
                .map(entity -> AdminProductMapper.toDto(
                        entity, names.get(entity.getCategoryId())))
                .toList();
        return AdminProductCsvWriter.toCsv(rows);
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Transactional(readOnly = true)
    public ProductDto findBySlug(String slug) {
        ProductEntity product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ProductNotFoundException(slug));
        return toDto(product);
    }

    /** Acepta el ID numérico histórico y el slug usado por el frontend. */
    @Transactional(readOnly = true)
    public ProductDto findByIdOrSlug(String idOrSlug) {
        if (idOrSlug != null && idOrSlug.matches("\\d+")) {
            return findById(Long.valueOf(idOrSlug));
        }
        return findBySlug(idOrSlug);
    }

    public void delete(Long id) {
        ProductEntity product = findEntity(id);
        productRepository.delete(product);
    }

    private ProductEntity findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private ProductDto toDto(ProductEntity entity) {
        String categoryName = entity.getCategoryId() == null ? null
                : categoryRepository.findById(entity.getCategoryId())
                        .map(CategoryEntity::getName)
                        .orElse(null);
        return ProductMapper.toDto(entity, categoryName);
    }

    private Page<ProductDto> toDtos(Page<ProductEntity> page) {
        Map<Long, String> names = categoryNames(page.getContent());
        return page.map(entity -> ProductMapper.toDto(entity, names.get(entity.getCategoryId())));
    }

    private Map<Long, String> categoryNames(Collection<ProductEntity> products) {
        Set<Long> categoryIds = products.stream()
                .map(ProductEntity::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return new HashMap<>();
        }
        return StreamSupport.stream(
                        categoryRepository.findAllById(categoryIds).spliterator(), false)
                .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));
    }

    private static String normalize(String q) {
        return q == null || q.isBlank() ? null : q.trim();
    }

    /** Unknown status values are ignored so the pill filter stays forgiving. */
    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        String value = status.trim().toLowerCase(Locale.ROOT);
        return Set.of(AdminProductMapper.STATUS_PUBLICADO,
                AdminProductMapper.STATUS_BORRADOR,
                AdminProductMapper.STATUS_AGOTADO).contains(value) ? value : null;
    }

    /** Omitting the flag in create/edit keeps the historical published-by-default behavior. */
    private static boolean resolvePublished(Boolean published) {
        return published == null || published;
    }

    private CategoryEntity validateCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    }

    private static String resolveSlug(String requested, String title, String sku,
                                      String existing) {
        if (requested != null && !requested.isBlank()) {
            return slugify(requested);
        }
        if (existing != null && !existing.isBlank()) {
            return existing;
        }
        String source = title != null && !title.isBlank() ? title : sku;
        return slugify(source == null ? "producto" : source);
    }

    private static String slugify(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "producto" : normalized;
    }

    private static <T> List<T> orEmpty(List<T> values) {
        return values == null ? List.of() : values;
    }
}
