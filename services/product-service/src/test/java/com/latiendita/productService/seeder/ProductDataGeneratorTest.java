package com.latiendita.productService.seeder;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.productService.seeder.model.SeedDataset;
import com.latiendita.productService.seeder.model.SeedProduct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

class ProductDataGeneratorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldGenerateDeterministicDataset() {
        SeedDataset first = generator(25, 42, "SEED").generate();
        SeedDataset second = generator(25, 42, "SEED").generate();

        assertThat(second).isEqualTo(first);
    }

    @Test
    void shouldChangeDatasetWhenSeedChanges() {
        SeedDataset first = generator(25, 42, "SEED").generate();
        SeedDataset second = generator(25, 43, "SEED").generate();

        assertThat(second.products().get(0).title()).isNotEqualTo(first.products().get(0).title());
        assertThat(second.products().get(0).sku()).isEqualTo(first.products().get(0).sku());
    }

    @Test
    void shouldRespectCountAndBusinessKeys() {
        SeedDataset dataset = generator(1000, 12345, "SEED").generate();

        assertThat(dataset.products()).hasSize(1000);
        assertThat(dataset.categories()).hasSize(20);
        assertThat(unique(dataset.products().stream().map(SeedProduct::sku))).hasSize(1000);
        assertThat(unique(dataset.products().stream().map(SeedProduct::slug))).hasSize(1000);
        assertThat(unique(dataset.categories().stream().map(c -> c.name()))).hasSize(20);
        assertThat(dataset.products()).allSatisfy(product -> {
            assertThat(product.sku()).startsWith("SEED-SKU-");
            assertThat(product.slug()).startsWith("producto-seed-");
            assertThat(product.published()).isTrue();
            assertThat(product.quantity()).isGreaterThanOrEqualTo(0);
            assertThat(product.title()).hasSizeLessThanOrEqualTo(255);
            assertThat(product.imageUrl()).hasSizeLessThanOrEqualTo(500);
        });
    }

    @Test
    void shouldKeepEveryProductWithinACategoryAndProduceValidJson() throws JacksonException {
        SeedDataset dataset = generator(50, 7, "TEST").generate();
        Set<Long> categoryIds = dataset.categories().stream()
                .map(c -> c.id())
                .collect(Collectors.toSet());

        assertThat(dataset.products())
                .allSatisfy(product -> assertThat(categoryIds).contains(product.categoryId()));

        SeedProduct product = dataset.products().get(0);
        assertThat(readList(product.imagesJson())).isNotEmpty();
        assertThat(readList(product.descriptionJson())).hasSize(2);
        assertThat(readList(product.highlightsJson())).hasSize(2);
        assertThat(product.specsJson()).startsWith("[");
    }

    private static <T> Set<T> unique(java.util.stream.Stream<T> values) {
        return values.collect(Collectors.toSet());
    }

    private List<String> readList(String json) throws JacksonException {
        return mapper.readValue(json, new TypeReference<>() {});
    }

    private static ProductDataGenerator generator(int count, long seed, String prefix) {
        SeederProperties properties = new SeederProperties();
        properties.setCount(count);
        properties.setRandomSeed(seed);
        properties.setExternalIdPrefix(prefix);
        properties.setExternalIdBase(9_000_000L);
        properties.setBatchSize(25);
        return new ProductDataGenerator(properties);
    }
}
