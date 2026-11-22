package com.latiendita.productService.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.productService.TestcontainersConfiguration;
import com.latiendita.productService.product.ProductRepository;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Admin Categorias contract (S2/S3/S7): the extended DTO, the persisted slug,
 * the shared low-stock threshold and the protected delete. All assertions go
 * through the public HTTP interface; persisted effects are checked with SQL.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class CategoryAdminApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldCreateCategoryWithActiveImageUrlAndPersistedSlug() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Bebidas Frías","description":"Bebidas",
                                 "featured":false,"active":false,
                                 "imageUrl":"https://cdn.example/bebidas.png"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/categories/")))
                .andExpect(jsonPath("$.categoryId").isString())
                .andExpect(jsonPath("$.slug").value("bebidas-frias"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example/bebidas.png"))
                .andExpect(jsonPath("$.featured").value(false));

        long id = categoryRepository.findByName("Bebidas Frías").orElseThrow().getId();
        assertThat(selectString("slug", id)).isEqualTo("bebidas-frias");
        assertThat(selectString("image_url", id)).isEqualTo("https://cdn.example/bebidas.png");
        assertThat(selectBoolean("active", id)).isFalse();
    }

    @Test
    void shouldUpdateActiveAndImageUrlWhilePreservingStoredSlug() throws Exception {
        long id = createCategory("Electrónica");

        mockMvc.perform(put("/api/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Electrónica","description":"Desc","featured":false,
                                 "slug":"legacy-link","active":true,
                                 "imageUrl":"https://cdn.example/one.png"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("legacy-link"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example/one.png"));

        mockMvc.perform(put("/api/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Electro","description":"Desc","featured":false,
                                 "active":false,"imageUrl":"https://cdn.example/two.png"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("legacy-link"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example/two.png"));

        assertThat(selectString("slug", id)).isEqualTo("legacy-link");
        assertThat(selectBoolean("active", id)).isFalse();
        assertThat(selectString("image_url", id)).isEqualTo("https://cdn.example/two.png");
    }

    @Test
    void shouldMaterializeSlugLazilyWhenEditingLegacyRow() throws Exception {
        jdbcTemplate.update("insert into categories (name, description, version) values (?, ?, ?)",
                "Legacy Row", "Creada antes de slug", 1L);
        long id = jdbcTemplate.queryForObject(
                "select id from categories where name = ?", Long.class, "Legacy Row");
        assertThat(selectString("slug", id)).isNull();

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("legacy-row"))
                .andExpect(jsonPath("$.active").value(true));
        assertThat(selectString("slug", id)).isNull();

        mockMvc.perform(put("/api/categories/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Legacy Row","description":"Creada antes de slug",
                                 "featured":false,"active":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("legacy-row"));

        assertThat(selectString("slug", id)).isEqualTo("legacy-row");
    }

    @Test
    void shouldDeriveLowStockCountWithSharedThreshold() throws Exception {
        long id = createCategory("Stock");
        createProduct("SKU-LOW-1", "Low one", id, 2);
        createProduct("SKU-LOW-2", "Low two", id, 4);
        createProduct("SKU-OK-1", "Not low", id, 7);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].productCount").value(3))
                .andExpect(jsonPath("$.content[0].lowStockCount").value(2));
    }

    @Test
    void shouldRejectDeleteWith409WhenCategoryHasProducts() throws Exception {
        long id = createCategory("Con Productos");
        createProduct("SKU-BLOCK-1", "Bloqueante", id, 10);

        mockMvc.perform(delete("/api/categories/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Categoría en uso"))
                .andExpect(jsonPath("$.detail", containsString("productos")));

        assertThat(categoryRepository.findById(id)).isPresent();
        assertThat(productRepository.countByCategoryId(id)).isEqualTo(1L);
    }

    @Test
    void shouldDeleteWith204WhenCategoryHasNoProducts() throws Exception {
        long id = createCategory("Vacía");

        mockMvc.perform(delete("/api/categories/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(categoryRepository.findById(id)).isEmpty();
    }

    @Test
    void shouldFilterByQueryCaseInsensitivelyAndSortByName() throws Exception {
        createCategory("Alfa");
        createCategory("Alfalfa");
        createCategory("Beta");

        mockMvc.perform(get("/api/categories")
                        .param("q", "ALF")
                        .param("sort", "name,asc")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Alfa"))
                .andExpect(jsonPath("$.content[1].name").value("Alfalfa"));

        mockMvc.perform(get("/api/categories")
                        .param("q", "beta")
                        .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Beta"));
    }

    @Test
    void shouldKeepUnfilteredCategoryListContract() throws Exception {
        createCategory("Electrónica");
        createCategory("Ropa");

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].categoryId").isString())
                .andExpect(jsonPath("$.content[0].name").value("Electrónica"))
                .andExpect(jsonPath("$.content[1].name").value("Ropa"))
                .andExpect(jsonPath("$.content[0].slug").value("electronica"))
                .andExpect(jsonPath("$.content[0].featured").value(false))
                .andExpect(jsonPath("$.content[0].active").value(true))
                .andExpect(jsonPath("$.content[0].imageUrl").value(nullValue()))
                .andExpect(jsonPath("$.content[0].lowStockCount").value(0))
                .andExpect(jsonPath("$.content[0].productCount").value(0));
    }

    @Test
    void shouldKeepDuplicateNameAndInvalidSortErrors() throws Exception {
        createCategory("Única");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Única","description":"Otra"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Recurso duplicado"));

        mockMvc.perform(get("/api/categories").param("sort", "noExiste"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Sort inválido"));
    }

    private long createCategory(String name) throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","description":"Desc","featured":false}
                                """.formatted(name)))
                .andExpect(status().isCreated());
        return categoryRepository.findByName(name).orElseThrow().getId();
    }

    private void createProduct(String sku, String title, long categoryId, int quantity)
            throws Exception {
        String slug = sku.toLowerCase(Locale.ROOT).replace('_', '-');
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"%s","title":"%s","slug":"%s","price":10.0,
                                 "quantity":%d,"categoryId":%d,"featured":false,"images":[]}
                                """.formatted(sku, title, slug, quantity, categoryId)))
                .andExpect(status().isCreated());
    }

    private String selectString(String column, long categoryId) {
        return jdbcTemplate.queryForObject(
                "select " + column + " from categories where id = ?", String.class, categoryId);
    }

    private boolean selectBoolean(String column, long categoryId) {
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "select " + column + " from categories where id = ?", Boolean.class, categoryId));
    }
}
