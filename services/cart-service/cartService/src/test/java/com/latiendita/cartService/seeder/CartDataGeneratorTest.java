package com.latiendita.cartService.seeder;

import static org.assertj.core.api.Assertions.assertThat;

import com.latiendita.cartService.seeder.model.SeedDataset;
import com.latiendita.cartService.seeder.model.SeededCart;
import com.latiendita.cartService.seeder.model.SeededCartItem;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class CartDataGeneratorTest {

    @Test
    void shouldGenerateDeterministicDataset() {
        assertThat(generator(25, 42, "SEED").generate())
                .isEqualTo(generator(25, 42, "SEED").generate());
    }

    @Test
    void shouldChangePricesWhenSeedChangesButKeepBusinessKeys() {
        SeedDataset first = generator(25, 42, "SEED").generate();
        SeedDataset second = generator(25, 43, "SEED").generate();

        assertThat(second.items().get(0).unitPrice())
                .isNotEqualTo(first.items().get(0).unitPrice());
        assertThat(second.carts().get(0).id()).isEqualTo(first.carts().get(0).id());
        assertThat(second.carts().get(0).userId()).isEqualTo(first.carts().get(0).userId());
    }

    @Test
    void shouldRespectCountUniqueKeysAndBusinessInvariants() {
        SeedDataset dataset = generator(1000, 12345, "SEED").generate();

        assertThat(dataset.carts()).hasSize(1000);
        assertThat(dataset.items()).hasSize(1999);
        assertThat(unique(dataset.carts().stream().map(SeededCart::id))).hasSize(1000);
        assertThat(unique(dataset.carts().stream().map(SeededCart::userId))).hasSize(1000);
        assertThat(unique(dataset.items().stream().map(SeededCartItem::id))).hasSize(1999);
        assertThat(dataset.carts()).allSatisfy(cart -> {
            assertThat(cart.userId()).hasSize(36);
            assertThat(cart.version()).isZero();
            assertThat(cart.updatedAt()).isAfter(cart.createdAt());
        });
        assertThat(dataset.items()).allSatisfy(item -> {
            assertThat(item.productId()).isBetween(9_100_000L, 9_100_999L);
            assertThat(item.quantity()).isBetween(1, 99);
            assertThat(item.unitPrice()).isPositive();
            assertThat(item.productTitle()).startsWith("Producto sintético SEED-SKU-");
            assertThat(item.imageUrl()).contains("/images/SEED-SKU-").hasSizeLessThan(500);
            assertThat(item.updatedAt()).isAfter(item.createdAt());
        });
        assertThat(dataset.items().stream().filter(SeededCartItem::priceChanged).count())
                .isEqualTo(100);

        Set<String> pairs = dataset.items().stream()
                .map(item -> item.cartId() + ":" + item.productId())
                .collect(Collectors.toSet());
        assertThat(pairs).hasSize(1999);
        assertThat(dataset.items()).allSatisfy(item ->
                assertThat(dataset.carts()).anyMatch(cart -> cart.id().equals(item.cartId())));
    }

    @Test
    void shouldDeriveCrossServiceIdsFromPrefixAndBase() {
        SeedDataset dataset = generator(5, 42, "SEED").generate();
        UUID expectedCustomer = UUID.nameUUIDFromBytes(
                "SEED-CUST-9000001".getBytes(StandardCharsets.UTF_8));

        assertThat(dataset.carts().get(0).userId()).isEqualTo(expectedCustomer.toString());
        assertThat(dataset.items().get(0).productId()).isEqualTo(9_100_000L);
        assertThat(dataset.items().get(0).productTitle())
                .isEqualTo("Producto sintético SEED-SKU-100000");
        assertThat(generator(5, 42, "OTHER").generate().carts().get(0).userId())
                .isNotEqualTo(expectedCustomer.toString());
    }

    private static <T> Set<T> unique(java.util.stream.Stream<T> values) {
        return values.collect(Collectors.toSet());
    }

    private static CartDataGenerator generator(int count, long seed, String prefix) {
        SeederProperties properties = new SeederProperties();
        properties.setCount(count);
        properties.setRandomSeed(seed);
        properties.setExternalIdPrefix(prefix);
        properties.setExternalIdBase(9_000_000L);
        properties.setBatchSize(25);
        return new CartDataGenerator(properties);
    }
}
