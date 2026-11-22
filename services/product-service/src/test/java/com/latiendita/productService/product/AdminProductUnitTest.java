package com.latiendita.productService.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.productService.category.CategoryRepository;
import com.latiendita.productService.product.dto.AdminProductCsvWriter;
import com.latiendita.productService.product.dto.AdminProductMapper;
import com.latiendita.productService.product.dto.AdminProductResponse;
import com.latiendita.productService.product.dto.ProductDto;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminProductUnitTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldDeriveAgotadoWhenQuantityIsNotPositive() {
        assertThat(AdminProductMapper.status(0, true)).isEqualTo("agotado");
        assertThat(AdminProductMapper.status(-3, false)).isEqualTo("agotado");
        assertThat(AdminProductMapper.status(null, true)).isEqualTo("agotado");
    }

    @Test
    void shouldDerivePublicadoWhenInStockAndPublished() {
        assertThat(AdminProductMapper.status(1, true)).isEqualTo("publicado");
        assertThat(AdminProductMapper.status(25, true)).isEqualTo("publicado");
    }

    @Test
    void shouldDeriveBorradorWhenInStockAndNotPublished() {
        assertThat(AdminProductMapper.status(1, false)).isEqualTo("borrador");
        assertThat(AdminProductMapper.status(25, null)).isEqualTo("borrador");
    }

    @Test
    void shouldDefaultPublishedToTrueOnEntityAndBuilder() {
        assertThat(new ProductEntity().getPublished()).isTrue();
        assertThat(ProductEntity.builder().build().getPublished()).isTrue();
    }

    @Test
    void shouldMapEntityToAdminResponseUsingStringIdAndDerivedStatus() {
        ProductEntity entity = ProductEntity.builder()
                .id(42L)
                .title("Remera")
                .sku("SKU-42")
                .price(new BigDecimal("1250.50"))
                .quantity(4)
                .published(false)
                .build();

        AdminProductResponse response = AdminProductMapper.toDto(entity, "Ropa");

        assertThat(response.productId()).isEqualTo("42");
        assertThat(response.name()).isEqualTo("Remera");
        assertThat(response.sku()).isEqualTo("SKU-42");
        assertThat(response.category()).isEqualTo("Ropa");
        assertThat(response.price()).isEqualByComparingTo("1250.50");
        assertThat(response.stock()).isEqualTo(4);
        assertThat(response.status()).isEqualTo("borrador");
        assertThat(response.published()).isFalse();
    }

    @Test
    void shouldNotExposePublishedInCatalogDtoPayload() throws Exception {
        ProductDto dto = new ProductDto(1L, "Remera", null, "SKU-1",
                new BigDecimal("10.00"), 5, null).withPublished(false);

        String json = new tools.jackson.databind.ObjectMapper().writeValueAsString(dto);

        assertThat(json).doesNotContain("published");
        assertThat(json).contains("\"id\":\"1\"");
    }

    @Test
    void shouldQuoteAndEscapeCsvFieldsFollowingRfc4180() {
        AdminProductResponse row = new AdminProductResponse("7", "Remera, \"Lino\"", "SKU-7",
                "Ropa", new BigDecimal("10.00"), 0, "agotado", false);

        String csv = AdminProductCsvWriter.toCsv(List.of(row));

        assertThat(csv).startsWith("productId,name,sku,category,price,stock,status,published\r\n");
        assertThat(csv).contains("\"Remera, \"\"Lino\"\"\"");
        assertThat(csv).contains("\"7\",\"Remera, \"\"Lino\"\"\",\"SKU-7\",\"Ropa\"");
        assertThat(csv).endsWith("\r\n");
    }

    @Test
    void shouldDefaultPublishedToTrueOnCreateWhenOmitted() {
        when(productRepository.existsBySku("SKU-NEW")).thenReturn(false);
        when(productRepository.existsBySlug("new")).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productService.create(new ProductDto(null, "New", null, "SKU-NEW", null, 5, null));

        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getPublished()).isTrue();
    }

    @Test
    void shouldPersistPublishedFalseOnCreateWhenRequested() {
        when(productRepository.existsBySku("SKU-NEW")).thenReturn(false);
        when(productRepository.existsBySlug("new")).thenReturn(false);
        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productService.create(new ProductDto(null, "New", null, "SKU-NEW", null, 5, null)
                .withPublished(false));

        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getPublished()).isFalse();
    }

    @Test
    void shouldPersistPublishedFalseOnUpdateWhenRequested() {
        ProductEntity existing = ProductEntity.builder()
                .id(1L).sku("SKU-1").slug("viejo").published(true).build();
        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(existing));
        when(productRepository.save(any(ProductEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        productService.update(1L, new ProductDto(1L, "Editado", null, "SKU-1", null, 5, null)
                .withPublished(false));

        ArgumentCaptor<ProductEntity> captor = ArgumentCaptor.forClass(ProductEntity.class);
        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getPublished()).isFalse();
    }
}
