package com.latiendita.productService.category;

import com.latiendita.productService.category.dto.CategoryDto;
import com.latiendita.productService.category.dto.CategoryMapper;
import com.latiendita.productService.exception.CategoryHasProductsException;
import com.latiendita.productService.exception.CategoryNotFoundException;
import com.latiendita.productService.exception.DuplicateCategoryException;
import com.latiendita.productService.product.ProductRepository;
import com.latiendita.productService.product.ProductService;
import com.latiendita.productService.sort.SortValidator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    /**
     * Propiedades de {@link CategoryEntity} que se pueden usar en `sort`. Cualquier
     * otra (o un formato como `-id`) se rechaza con 400 antes de llegar a JPA.
     */
    private static final Set<String> SORTABLE_PROPERTIES =
            Set.of("id", "name", "description", "featured");

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryDto create(CategoryDto dto) {
        if (categoryRepository.existsByName(dto.name())) {
            throw new DuplicateCategoryException(dto.name());
        }

        CategoryEntity saved = categoryRepository.save(CategoryEntity.builder()
                .name(dto.name())
                .description(dto.description())
                .featured(dto.featured())
                .slug(CategoryMapper.slugify(dto.name()))
                .active(dto.active())
                .imageUrl(dto.imageUrl())
                .build());

        return toDto(saved);
    }

    public CategoryDto update(Long id, CategoryDto dto) {
        CategoryEntity category = findEntity(id);

        boolean nameChanged = !category.getName().equals(dto.name());
        if (nameChanged && categoryRepository.existsByNameAndIdNot(dto.name(), id)) {
            throw new DuplicateCategoryException(dto.name());
        }

        category.setName(dto.name());
        category.setDescription(dto.description());
        category.setFeatured(dto.featured());
        category.setActive(dto.active());
        category.setImageUrl(dto.imageUrl());
        category.setSlug(resolveUpdatedSlug(category, dto));

        CategoryEntity saved = categoryRepository.save(category);

        return toDto(saved);
    }

    /** An explicit slug wins; otherwise keep the stored one or materialize it. */
    private String resolveUpdatedSlug(CategoryEntity category, CategoryDto dto) {
        if (dto.slug() != null && !dto.slug().isBlank()) {
            return dto.slug();
        }
        return CategoryMapper.resolveSlug(category);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDto> findAll(Pageable pageable) {
        return findAll(null, null, pageable);
    }

    /** Sin filtro se conserva el listado histórico; con filtro se aplica en la BD. */
    @Transactional(readOnly = true)
    public Page<CategoryDto> findAll(Boolean featured, Pageable pageable) {
        return findAll(null, featured, pageable);
    }

    /**
     * `q` busca por nombre (substring, sin distinguir mayúsculas) y se puede
     * combinar con `featured`. Sin filtros se usan los repositorios históricos.
     */
    @Transactional(readOnly = true)
    public Page<CategoryDto> findAll(String q, Boolean featured, Pageable pageable) {
        SortValidator.validate(pageable.getSort(), SORTABLE_PROPERTIES);
        Page<CategoryEntity> categories;
        if (q == null || q.isBlank()) {
            categories = featured == null
                    ? categoryRepository.findAll(pageable)
                    : categoryRepository.findByFeatured(featured, pageable);
        } else {
            categories = categoryRepository.search(q.trim(), featured, pageable);
        }
        return categories.map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDto> findAllPaging(int page, int size) {
        return categoryRepository.findAll(PageRequest.of(page, size)).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CategoryDto> findAllPagingAndSorting(int pageNo, int pageSize, String sortBy) {
        String property = "categoryId".equals(sortBy) ? "id" : sortBy;
        Sort sort = Sort.by(property);
        SortValidator.validate(sort, SORTABLE_PROPERTIES);
        return categoryRepository
                .findAll(PageRequest.of(pageNo, pageSize, sort))
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        return toDto(findEntity(id));
    }

    @Transactional(readOnly = true)
    public CategoryDto findBySlug(String slug) {
        CategoryEntity category = categoryRepository.findAll().stream()
                .filter(entity -> CategoryMapper.resolveSlug(entity).equals(slug))
                .findFirst()
                .orElseThrow(() -> new CategoryNotFoundException(slug));
        return toDto(category);
    }

    /** Acepta el ID numérico histórico y el slug derivado que usa el frontend. */
    @Transactional(readOnly = true)
    public CategoryDto findByIdOrSlug(String idOrSlug) {
        if (idOrSlug != null && idOrSlug.matches("-?\\d+")) {
            return findById(Long.valueOf(idOrSlug));
        }
        return findBySlug(idOrSlug);
    }

    /** Borrado protegido: con productos asociados responde 409 y no toca nada. */
    public void delete(Long id) {
        CategoryEntity category = findEntity(id);
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new CategoryHasProductsException(category.getName(), productCount);
        }
        categoryRepository.delete(category);
    }

    private CategoryDto toDto(CategoryEntity entity) {
        boolean persisted = entity.getId() != null;
        long productCount = persisted
                ? productRepository.countByCategoryId(entity.getId())
                : 0L;
        long lowStockCount = persisted
                ? categoryRepository.countLowStockByCategoryId(
                        entity.getId(), ProductService.LOW_STOCK_THRESHOLD)
                : 0L;
        return CategoryMapper.toDto(entity, productCount, lowStockCount);
    }

    private CategoryEntity findEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }
}
