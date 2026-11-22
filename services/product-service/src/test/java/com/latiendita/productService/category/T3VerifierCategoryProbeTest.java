package com.latiendita.productService.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
class T3VerifierCategoryProbeTest {

    @Autowired private MockMvc mvc;
    @Autowired private CategoryRepository categories;
    @Autowired private ProductRepository products;
    @Autowired private JdbcTemplate jdbc;

    @BeforeEach void clean() { products.deleteAll(); categories.deleteAll(); }

    @Test void probe_defaults_and_string_id() throws Exception {
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bebida Tibia\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.categoryId").isString())
                .andExpect(jsonPath("$.slug").value("bebida-tibia"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.imageUrl").value(nullValue()))
                .andExpect(jsonPath("$.featured").value(false))
                .andExpect(jsonPath("$.productCount").value(0))
                .andExpect(jsonPath("$.lowStockCount").value(0));
        long id = categories.findByName("Bebida Tibia").orElseThrow().getId();
        mvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(String.valueOf(id)));
    }

    @Test void probe_q_is_case_insensitive_and_combines_with_featured() throws Exception {
        create("Café", true); create("cafecito", false); create("Té", true);
        mvc.perform(get("/api/categories").param("q", "CAF").param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Café"))
                .andExpect(jsonPath("$.content[1].name").value("cafecito"));
        mvc.perform(get("/api/categories").param("q", "caf").param("featured", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Café"));
        mvc.perform(get("/api/categories").param("featured", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get("/api/categories").param("q", "nope"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test void probe_sort_allowlist_and_invalid_values() throws Exception {
        create("Alfa", false); create("Beta", false);
        mvc.perform(get("/api/categories").param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alfa"));
        mvc.perform(get("/api/categories").param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Beta"));
        mvc.perform(get("/api/categories").param("sort", "id,asc"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/categories").param("sort", "categoryId,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Sort inválido"));
        mvc.perform(get("/api/categories").param("sort", "-name"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Sort inválido"));
    }

    @Test void probe_unfiltered_contract_still_paginates() throws Exception {
        create("X", false); create("Y", false);
        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].categoryId").isString());
        mvc.perform(get("/api/categories").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test void probe_slug_preserved_on_rename_and_backfilled_on_legacy_row() throws Exception {
        long id = create("Mesa", false);
        mvc.perform(put("/api/categories/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mesa Grande\",\"slug\":\"legacy-link\","
                                + "\"active\":false,\"imageUrl\":\"https://cdn/x.png\","
                                + "\"featured\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("legacy-link"))
                .andExpect(jsonPath("$.active").value(false));
        mvc.perform(put("/api/categories/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mesa Nueva\",\"slug\":\"  \","
                                + "\"active\":true,\"featured\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("legacy-link"));
        assertThat(jdbc.queryForObject("select slug from categories where id = ?",
                String.class, id)).isEqualTo("legacy-link");

        jdbc.update("insert into categories (name, description, version) values (?, ?, ?)",
                "Legacy Row", "sin slug", 1L);
        long legacy = jdbc.queryForObject("select id from categories where name = ?",
                Long.class, "Legacy Row");
        mvc.perform(get("/api/categories/{id}", legacy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("legacy-row"));
        assertThat(jdbc.queryForObject("select slug from categories where id = ?",
                String.class, legacy)).isNull();
        mvc.perform(put("/api/categories/{id}", legacy).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Legacy Row\",\"active\":true,\"featured\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("legacy-row"));
        assertThat(jdbc.queryForObject("select slug from categories where id = ?",
                String.class, legacy)).isEqualTo("legacy-row");
    }

    @Test void probe_low_stock_boundary_counts_only_quantity_below_five() throws Exception {
        long id = create("Umbral", false);
        makeProduct("P0", id, 0); makeProduct("P4", id, 4);
        makeProduct("P5", id, 5); makeProduct("P6", id, 6);
        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productCount").value(4))
                .andExpect(jsonPath("$.content[0].lowStockCount").value(2));
    }

    @Test void probe_delete_guard_keeps_state_byte_identical_then_allows_empty() throws Exception {
        long id = create("Bloqueada", false);
        makeProduct("BQ", id, 3);
        String before = snapshot();
        mvc.perform(delete("/api/categories/{id}", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Categoría en uso"))
                .andExpect(jsonPath("$.detail", containsString("productos")));
        assertThat(snapshot()).isEqualTo(before);
        assertThat(categories.findById(id)).isPresent();
        assertThat(products.countByCategoryId(id)).isEqualTo(1L);

        products.deleteAll();
        mvc.perform(delete("/api/categories/{id}", id))
                .andExpect(status().isNoContent());
        assertThat(categories.findById(id)).isEmpty();
    }

    @Test void probe_validation_missing_and_invalid_inputs() throws Exception {
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"));
        mvc.perform(delete("/api/categories/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Categoría no encontrada"));
        mvc.perform(get("/api/categories/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Categoría no encontrada"));
        mvc.perform(put("/api/categories/999999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fantasma\",\"featured\":false}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Categoría no encontrada"));
    }

    private long create(String name, boolean featured) throws Exception {
        mvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"featured\":" + featured + "}"))
                .andExpect(status().isCreated());
        return categories.findByName(name).orElseThrow().getId();
    }

    private void makeProduct(String sku, long categoryId, int quantity) throws Exception {
        mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"" + sku + "\",\"title\":\"" + sku + "\","
                                + "\"slug\":\"" + sku.toLowerCase(Locale.ROOT) + "\","
                                + "\"price\":10.0,\"quantity\":" + quantity
                                + ",\"categoryId\":" + categoryId
                                + ",\"featured\":false,\"images\":[]}"))
                .andExpect(status().isCreated());
    }

    private String snapshot() {
        return jdbc.queryForList("select * from categories order by id").toString()
                + "|" + jdbc.queryForList("select * from products order by id").toString();
    }
}
