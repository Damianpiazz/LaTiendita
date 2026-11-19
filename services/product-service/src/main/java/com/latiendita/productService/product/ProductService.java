package com.latiendita.productService.product;

import com.latiendita.productService.category.CategoryEntity;
import com.latiendita.productService.category.CategoryRepository;
import com.latiendita.productService.exception.CategoryNotFoundException;
import com.latiendita.productService.exception.DuplicateSkuException;
import com.latiendita.productService.exception.DuplicateSlugException;
import com.latiendita.productService.exception.ProductNotFoundException;
import com.latiendita.productService.product.dto.ProductDto;
import com.latiendita.productService.product.dto.ProductMapper;
import com.latiendita.productService.sort.SortValidator;
import java.text.Normalizer;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    /**
     * Propiedades de {@link ProductEntity} que se pueden usar en `sort`. Cualquier
     * otra (o un formato como `-id`) se rechaza con 400 antes de llegar a JPA.
     */
    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
            "id", "title", "imageUrl", "sku", "price", "quantity", "categoryId", "slug",
            "subcategory", "featured", "originalPrice", "fulfillment");

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
        String term = q == null || q.isBlank() ? null : q.trim();
        return toDtos(productRepository.search(term, categoryId, featured, pageable));
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
        Set<Long> categoryIds = page.getContent().stream()
                .map(ProductEntity::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryNames = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryNames = StreamSupport.stream(
                            categoryRepository.findAllById(categoryIds).spliterator(), false)
                    .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));
        }

        final Map<Long, String> names = categoryNames;
        return page.map(entity -> ProductMapper.toDto(entity, names.get(entity.getCategoryId())));
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
