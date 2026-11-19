package com.latiendita.productService.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.productService.category.CategoryEntity;
import com.latiendita.productService.category.CategoryRepository;
import com.latiendita.productService.exception.DuplicateSlugException;
import com.latiendita.productService.product.dto.JsonListConverter;
import com.latiendita.productService.product.dto.JsonSpecsConverter;
import com.latiendita.productService.product.dto.ProductDto;
import com.latiendita.productService.product.dto.ProductSpec;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ProductCatalogModelTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProductWithCatalogAndDetailFields() {
        ProductDto request = catalogDto(null, "Auriculares Inalámbricos",
                "auriculares-inalambricos", 1L);

        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(productRepository.existsBySlug("auriculares-inalambricos")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(
                CategoryEntity.builder().id(1L).name("Tecnología").build()));
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> {
            ProductEntity saved = invocation.getArgument(0);
            saved.setId(42L);
            return saved;
        });

        ProductDto response = productService.create(request);

        assertThat(response.productId()).isEqualTo(42L);
        assertThat(response.getId()).isEqualTo("42");
        assertThat(response.title()).isEqualTo("Auriculares Inalámbricos");
        assertThat(response.getName()).isEqualTo("Auriculares Inalámbricos");
        assertThat(response.slug()).isEqualTo("auriculares-inalambricos");
        assertThat(response.categoryName()).isEqualTo("Tecnología");
        assertThat(response.subcategory()).isEqualTo("Audio");
        assertThat(response.featured()).isTrue();
        assertThat(response.originalPrice()).isEqualByComparingTo("32500");
        assertThat(response.fulfillment()).isEqualTo("24/48 hs");
        assertThat(response.images()).containsExactly("main.png", "side.png");
        assertThat(response.getGalleryCount()).isEqualTo(2);
        assertThat(response.description()).containsExactly("Un párrafo");
        assertThat(response.highlights()).containsExactly("Cancelación activa");
        assertThat(response.specs()).containsExactly(
                new ProductSpec("Marca", "SoundCore"));

        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("auriculares-inalambricos");
        assertThat(captor.getValue().getImages())
                .containsExactly("main.png", "side.png");
        assertThat(captor.getValue().getSpecs())
                .containsExactly(new ProductSpec("Marca", "SoundCore"));
    }

    @Test
    void shouldGenerateSlugFromTitleWhenMissing() {
        ProductDto request = catalogDto(null, "Lámpara de Mesa!", null, null);

        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(productRepository.existsBySlug("lampara-de-mesa")).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class))).thenAnswer(i -> i.getArgument(0));

        ProductDto response = productService.create(request);

        assertThat(response.slug()).isEqualTo("lampara-de-mesa");
        verify(productRepository).existsBySlug("lampara-de-mesa");
    }

    @Test
    void shouldRejectDuplicateSlugOnCreate() {
        when(productRepository.existsBySku("SKU-1")).thenReturn(false);
        when(productRepository.existsBySlug("auriculares-inalambricos")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(
                catalogDto(null, "Auriculares Inalámbricos", "auriculares-inalambricos", null)))
                .isInstanceOf(DuplicateSlugException.class)
                .hasMessageContaining("auriculares-inalambricos");

        verify(productRepository, never()).save(any());
    }

    @Test
    void shouldFindProductBySlug() {
        ProductEntity entity = ProductEntity.builder()
                .id(42L)
                .title("Auriculares Inalámbricos")
                .sku("SKU-1")
                .slug("auriculares-inalambricos")
                .featured(true)
                .images(List.of("main.png", "side.png"))
                .build();
        when(productRepository.findBySlug("auriculares-inalambricos"))
                .thenReturn(Optional.of(entity));

        ProductDto response = productService.findBySlug("auriculares-inalambricos");

        assertThat(response.productId()).isEqualTo(42L);
        assertThat(response.slug()).isEqualTo("auriculares-inalambricos");
        assertThat(response.getGalleryCount()).isEqualTo(2);
    }

    @Test
    void shouldFilterByFeaturedFlag() {
        Pageable pageable = PageRequest.of(0, 8);
        ProductEntity entity = ProductEntity.builder()
                .id(42L)
                .title("Auriculares Inalámbricos")
                .sku("SKU-1")
                .slug("auriculares-inalambricos")
                .featured(true)
                .build();
        when(productRepository.search(null, null, true, pageable))
                .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

        assertThat(productService.search(null, null, true, pageable)).hasSize(1);
        verify(productRepository).search(null, null, true, pageable);
    }

    @Test
    void shouldSerializeCatalogAliasesAndDetailJson() throws Exception {
        ProductDto dto = catalogDto(42L, "Auriculares Inalámbricos",
                "auriculares-inalambricos", 1L);

        String json = new tools.jackson.databind.ObjectMapper().writeValueAsString(dto);

        assertThat(json).contains("\"id\":\"42\"");
        assertThat(json).contains("\"name\":\"Auriculares Inalámbricos\"");
        assertThat(json).contains("\"stock\":5");
        assertThat(json).contains("\"slug\":\"auriculares-inalambricos\"");
        assertThat(json).contains("\"featured\":true");
        assertThat(json).contains("\"images\":[\"main.png\",\"side.png\"]");
        assertThat(json).contains("\"specs\":[{\"label\":\"Marca\",\"value\":\"SoundCore\"}]");
    }

    @Test
    void shouldRoundTripImagesJson() {
        JsonListConverter converter = new JsonListConverter();

        String json = converter.convertToDatabaseColumn(List.of("main.png", "side.png"));

        assertThat(json).isEqualTo("[\"main.png\",\"side.png\"]");
        assertThat(converter.convertToEntityAttribute(json))
                .containsExactly("main.png", "side.png");
    }

    @Test
    void shouldRoundTripSpecsJson() {
        JsonSpecsConverter converter = new JsonSpecsConverter();
        List<ProductSpec> specs = List.of(
                new ProductSpec("Marca", "SoundCore"),
                new ProductSpec("Modelo", "NC-450"));

        String json = converter.convertToDatabaseColumn(specs);

        assertThat(json).contains("\"label\":\"Marca\"", "\"value\":\"NC-450\"");
        assertThat(converter.convertToEntityAttribute(json)).containsExactlyElementsOf(specs);
    }

    private static ProductDto catalogDto(Long id, String title, String slug, Long categoryId) {
        return new ProductDto(
                id,
                title,
                "main.png",
                "SKU-1",
                new BigDecimal("25999"),
                5,
                categoryId,
                slug,
                null,
                "Audio",
                true,
                new BigDecimal("32500"),
                "24/48 hs",
                List.of("main.png", "side.png"),
                List.of("Un párrafo"),
                List.of("Cancelación activa"),
                List.of(new ProductSpec("Marca", "SoundCore")));
    }
}
