package com.latiendita.productService.home;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.productService.TestcontainersConfiguration;
import com.latiendita.productService.category.CategoryEntity;
import com.latiendita.productService.category.CategoryRepository;
import com.latiendita.productService.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Home catalog contract: both featured GETs, their exact JSON fields, and the
 * unchanged no-filter category list. Requests are sent as raw JSON so the tests
 * exercise the public HTTP contract instead of a Java DTO.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class HomeCatalogApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void cleanDatabase() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void shouldReturnFeaturedCategoriesWithHomeFields() throws Exception {
        long technologyId = createCategory("Tecnología", "Dispositivos", true);
        createCategory("Hogar", "Para la casa", false);
        createProduct("SKU-HOME-1", "Notebook", "notebook", technologyId, true);

        mockMvc.perform(get("/api/categories").param("featured", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].categoryId")
                        .value(String.valueOf(technologyId)))
                .andExpect(jsonPath("$.content[0].name").value("Tecnología"))
                .andExpect(jsonPath("$.content[0].description").value("Dispositivos"))
                .andExpect(jsonPath("$.content[0].slug").value("tecnologia"))
                .andExpect(jsonPath("$.content[0].featured").value(true))
                .andExpect(jsonPath("$.content[0].productCount").value(1));
    }

    @Test
    void shouldReturnAllCategoriesWhenFeaturedFilterIsAbsent() throws Exception {
        createCategory("Electrónica", "Devices", true);
        createCategory("Ropa", "Indumentaria", false);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Electrónica"))
                .andExpect(jsonPath("$.content[1].name").value("Ropa"));
    }

    @Test
    void shouldReturnFeaturedProductsKeepingExistingAliases() throws Exception {
        long technologyId = createCategory("Tecnología", "Dispositivos", false);
        createProduct("SKU-HOME-1", "Notebook", "notebook", technologyId, true);
        createProduct("SKU-HOME-2", "Taza", "taza", technologyId, false);

        mockMvc.perform(get("/api/products").param("featured", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").isString())
                .andExpect(jsonPath("$.content[0].name").value("Notebook"))
                .andExpect(jsonPath("$.content[0].price").value(999.99))
                .andExpect(jsonPath("$.content[0].slug").value("notebook"))
                .andExpect(jsonPath("$.content[0].featured").value(true))
                .andExpect(jsonPath("$.content[0].stock").value(5))
                .andExpect(jsonPath("$.content[0].galleryCount").value(2));
    }

    @Test
    void shouldToggleFeaturedThroughExistingUpdateEndpoint() throws Exception {
        long categoryId = createCategory("Hogar", "Para la casa", false);

        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoryId":%d,"name":"Hogar","description":"Para la casa",
                                 "slug":"hogar","featured":true,"productCount":0}
                                """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featured").value(true))
                .andExpect(jsonPath("$.slug").value("hogar"));

        mockMvc.perform(get("/api/categories").param("featured", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].categoryId")
                        .value(String.valueOf(categoryId)));
    }

    private long createCategory(String name, String description, boolean featured)
            throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","description":"%s","featured":%s}
                                """.formatted(name, description, featured)))
                .andExpect(status().isCreated());

        CategoryEntity category = categoryRepository.findByName(name).orElseThrow();
        return category.getId();
    }

    private void createProduct(String sku, String title, String slug, long categoryId,
                               boolean featured) throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"%s","title":"%s","slug":"%s","price":999.99,
                                 "quantity":5,"categoryId":%d,"featured":%s,
                                 "images":["main.png","side.png"]}
                                """.formatted(sku, title, slug, categoryId, featured)))
                .andExpect(status().isCreated());
    }
}
