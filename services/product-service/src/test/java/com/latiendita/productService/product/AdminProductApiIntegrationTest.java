package com.latiendita.productService.product;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.productService.TestcontainersConfiguration;
import com.latiendita.productService.category.CategoryEntity;
import com.latiendita.productService.category.CategoryRepository;
import com.latiendita.productService.product.dto.ProductIdsRequest;
import com.latiendita.productService.product.dto.ProductStatusUpdateRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class AdminProductApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldExposeAdminSummaryWithCountsAndThreshold() throws Exception {
        CategoryEntity ropa = categoryRepository.save(CategoryEntity.builder().name("Ropa").build());
        categoryRepository.save(CategoryEntity.builder().name("Hogar").build());
        productRepository.saveAll(List.of(
                product("SKU-1", "Remera", 8, true, ropa.getId()),
                product("SKU-2", "Taza", 2, true, null),
                product("SKU-3", "Jean", 0, true, ropa.getId())));

        mockMvc.perform(get("/api/products/admin/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(3))
                .andExpect(jsonPath("$.totalCategories").value(2))
                .andExpect(jsonPath("$.totalLowStock").value(2))
                .andExpect(jsonPath("$.lowStockThreshold").value(5));
    }

    @Test
    void shouldApplySummaryFilters() throws Exception {
        CategoryEntity ropa = categoryRepository.save(CategoryEntity.builder().name("Ropa").build());
        productRepository.saveAll(List.of(
                product("SKU-1", "Remera", 8, true, ropa.getId()),
                product("SKU-2", "Taza", 2, true, null)));

        mockMvc.perform(get("/api/products/admin/summary")
                        .param("q", "remera")
                        .param("categoryId", String.valueOf(ropa.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(1))
                .andExpect(jsonPath("$.totalLowStock").value(0));
    }

    @Test
    void shouldDeriveStatusForEachAdminRow() throws Exception {
        productRepository.saveAll(List.of(
                product("SKU-1", "Publicado", 5, true, null),
                product("SKU-2", "Borrador", 5, false, null),
                product("SKU-3", "Agotado", 0, true, null)));

        mockMvc.perform(get("/api/products/admin").param("sort", "sku,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].productId").isString())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$.content[0].status").value("publicado"))
                .andExpect(jsonPath("$.content[1].status").value("borrador"))
                .andExpect(jsonPath("$.content[2].status").value("agotado"))
                .andExpect(jsonPath("$.content[2].published").value(true));
    }

    @Test
    void shouldSearchAdminProductsByNameOrSku() throws Exception {
        productRepository.saveAll(List.of(
                product("SKU-ALPHA", "Remera", 5, true, null),
                product("SKU-OTHER", "Taza", 5, true, null)));

        mockMvc.perform(get("/api/products/admin").param("q", "alpha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("SKU-ALPHA"));
    }

    @Test
    void shouldFilterAdminProductsByStatusAndCategoryAndPaginate() throws Exception {
        CategoryEntity ropa = categoryRepository.save(CategoryEntity.builder().name("Ropa").build());
        productRepository.saveAll(List.of(
                product("SKU-1", "A", 5, true, ropa.getId()),
                product("SKU-2", "B", 5, false, ropa.getId()),
                product("SKU-3", "C", 5, false, null)));

        mockMvc.perform(get("/api/products/admin")
                        .param("status", "borrador")
                        .param("categoryId", String.valueOf(ropa.getId()))
                        .param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].sku").value("SKU-2"));

        mockMvc.perform(get("/api/products/admin").param("sort", "sku,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-3"));
    }

    @Test
    void shouldPublishAndArchiveProductsInBulk() throws Exception {
        productRepository.saveAll(List.of(
                product("SKU-1", "A", 5, false, null),
                product("SKU-2", "B", 5, false, null)));
        List<Long> ids = productRepository.findAll().stream().map(ProductEntity::getId).toList();

        mockMvc.perform(post("/api/products/admin/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductStatusUpdateRequest(ids, true))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/admin").param("status", "publicado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mockMvc.perform(post("/api/products/admin/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductStatusUpdateRequest(ids, false))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/admin").param("status", "borrador"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldRejectBulkStatusRequestWithEmptyIds() throws Exception {
        mockMvc.perform(post("/api/products/admin/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductStatusUpdateRequest(List.of(), true))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"));
    }

    @Test
    void shouldDeleteProductsInBulkWithoutSingleDeleteRouteShadowing() throws Exception {
        productRepository.saveAll(List.of(
                product("SKU-1", "A", 5, true, null),
                product("SKU-2", "B", 5, true, null)));
        List<Long> ids = productRepository.findAll().stream().map(ProductEntity::getId).toList();

        mockMvc.perform(delete("/api/products/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductIdsRequest(ids))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldExportCsvWithSameFiltersAndRfcQuoting() throws Exception {
        productRepository.save(product("SKU-1", "Remera, \"Lino\"", 5, false, null));
        productRepository.save(product("SKU-2", "Taza", 5, true, null));

        mockMvc.perform(get("/api/products/admin/export.csv").param("q", "SKU-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", startsWith("text/csv")))
                .andExpect(content().string(startsWith(
                        "productId,name,sku,category,price,stock,status,published\r\n")))
                .andExpect(content().string(containsString("\"Remera, \"\"Lino\"\"\"")))
                .andExpect(content().string(containsString("\"borrador\"")))
                .andExpect(content().string(not(containsString("SKU-2"))));
    }

    @Test
    void shouldAcceptPublishedOnCreate() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Borrador","sku":"SKU-NEW","price":10,"quantity":3,"featured":false,"published":false}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/products/admin").param("status", "borrador"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].published").value(false));
    }

    @Test
    void shouldAcceptPublishedOnUpdate() throws Exception {
        ProductEntity saved = productRepository.save(product("SKU-1", "Borrador", 3, false, null));

        mockMvc.perform(put("/api/products/{productId}", saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Publicado","sku":"SKU-1","price":10,"quantity":3,"featured":false,"published":true}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/admin").param("status", "publicado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].published").value(true));
    }

    @Test
    void shouldKeepPublicProductListContractWithoutFilters() throws Exception {
        productRepository.save(product("SKU-1", "Remera", 5, true, null));

        String json = mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").isString())
                .andExpect(jsonPath("$.content[0].name").value("Remera"))
                .andExpect(jsonPath("$.content[0].stock").value(5))
                .andExpect(jsonPath("$.content[0].featured").value(false))
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(json).doesNotContain("\"published\"");
    }

    @Test
    void shouldDeclareDefaultTrueForPublishedColumn() throws Exception {
        String defaultExpression = jdbcTemplate.queryForObject(
                "select column_default from information_schema.columns "
                        + "where table_name = 'products' and column_name = 'published'",
                String.class);

        org.assertj.core.api.Assertions.assertThat(defaultExpression)
                .isNotNull()
                .isEqualTo("true");
    }

    private ProductEntity product(String sku, String title, int quantity,
                                  boolean published, Long categoryId) {
        return ProductEntity.builder()
                .sku(sku)
                .title(title)
                .price(new BigDecimal("10.00"))
                .quantity(quantity)
                .categoryId(categoryId)
                .published(published)
                .build();
    }
}
