package com.latiendita.productService.category;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.productService.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * GET /api/categories/{categoryIdOrSlug} must accept both the historic numeric
 * id and the slug derived from the category name, as products already do.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class CategoryLookupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void cleanDatabase() {
        categoryRepository.deleteAll();
    }

    @Test
    void shouldFindCategoryByDerivedSlug() throws Exception {
        long categoryId = createCategory("Tecnología");

        mockMvc.perform(get("/api/categories/{categoryIdOrSlug}", "tecnologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(String.valueOf(categoryId)))
                .andExpect(jsonPath("$.name").value("Tecnología"))
                .andExpect(jsonPath("$.slug").value("tecnologia"));
    }

    @Test
    void shouldStillFindCategoryByNumericId() throws Exception {
        long categoryId = createCategory("Hogar");

        mockMvc.perform(get("/api/categories/{categoryIdOrSlug}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(String.valueOf(categoryId)))
                .andExpect(jsonPath("$.name").value("Hogar"))
                .andExpect(jsonPath("$.slug").value("hogar"));
    }

    @Test
    void shouldKeepIdWordingForNegativeNumericLookup() throws Exception {
        mockMvc.perform(get("/api/categories/{categoryIdOrSlug}", "-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Categoría no encontrada con id -1"));
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
}
