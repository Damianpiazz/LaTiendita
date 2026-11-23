package com.latiendita.productService.seeder;

import com.latiendita.productService.product.dto.ProductSpec;
import com.latiendita.productService.seeder.model.SeedCategory;
import com.latiendita.productService.seeder.model.SeedDataset;
import com.latiendita.productService.seeder.model.SeedProduct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Genera el catálogo sintético de forma determinista: la misma semilla,
 * el mismo `count` y el mismo prefijo producen siempre exactamente las mismas filas.
 * Datafaker aporta variedad realista; las claves de negocio (sku/slug) se
 * derivan del índice para garantizar UNIQUE sin colisiones.
 */
@Component
public class ProductDataGenerator {

    private static final List<String> CATEGORY_NAMES = List.of(
            "Electrónica", "Informática", "Hogar", "Moda", "Deportes", "Belleza",
            "Alimentación", "Jardín", "Automoción", "Libros", "Juguetes", "Música",
            "Oficina", "Salud", "Mascotas", "Cocina", "Viajes", "Educación",
            "Artesanía", "Bebés");
    private static final List<String> SUBCATEGORIES = List.of(
            "Destacados", "Nuevos", "Básicos", "Premium", "Ofertas", "Accesorios");
    private static final List<String> FULFILLMENT = List.of("delivery", "pickup");

    private static final int CATEGORY_OFFSET = 1;
    private static final int PRODUCT_OFFSET = 100_000;

    private final SeederProperties properties;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductDataGenerator(SeederProperties properties) {
        this.properties = properties;
    }

    public SeedDataset generate() {
        String prefix = properties.getExternalIdPrefix();
        long base = properties.getExternalIdBase();
        Faker faker = new Faker(new Random(properties.getRandomSeed()));

        List<SeedCategory> categories = new ArrayList<>(CATEGORY_NAMES.size());
        for (int i = 0; i < CATEGORY_NAMES.size(); i++) {
            String code = padding(CATEGORY_OFFSET + i);
            String name = prefix + "-CAT-" + code + " " + CATEGORY_NAMES.get(i);
            categories.add(new SeedCategory(
                    base + CATEGORY_OFFSET + i,
                    truncate(name, 100),
                    "categoria-" + prefix.toLowerCase(Locale.ROOT) + "-" + code,
                    "Categoría sintética de pruebas " + code,
                    true,
                    i % 5 == 0,
                    "https://images.example.test/" + prefix.toLowerCase(Locale.ROOT)
                            + "/categorias/" + code + ".jpg"));
        }

        List<SeedProduct> products = new ArrayList<>(properties.getCount());
        for (int i = 0; i < properties.getCount(); i++) {
            String code = padding(PRODUCT_OFFSET + i);
            String sku = prefix + "-SKU-" + code;
            String slug = "producto-" + prefix.toLowerCase(Locale.ROOT) + "-" + code;
            long categoryId = categories.get(i % categories.size()).id();
            BigDecimal price = BigDecimal.valueOf(
                    faker.number().numberBetween(199, 99_999), 2);
            BigDecimal originalPrice = price.multiply(BigDecimal.valueOf(1.20))
                    .setScale(2, RoundingMode.HALF_UP);
            String title = truncate(faker.commerce().productName(), 180) + " " + sku;
            String imageUrl = "https://images.example.test/"
                    + prefix.toLowerCase(Locale.ROOT) + "/productos/" + sku + ".jpg";

            products.add(new SeedProduct(
                    base + PRODUCT_OFFSET + i,
                    truncate(title, 255),
                    truncate(imageUrl, 500),
                    truncate(sku, 100),
                    price,
                    faker.number().numberBetween(0, 250),
                    categoryId,
                    truncate(slug, 255),
                    SUBCATEGORIES.get(i % SUBCATEGORIES.size()),
                    i % 7 == 0,
                    true,
                    originalPrice,
                    FULFILLMENT.get(i % FULFILLMENT.size()),
                    json(List.of(imageUrl)),
                    json(List.of("Descripción sintética " + code,
                            "Generada de forma determinista para pruebas")),
                    json(List.of("Calidad " + SUBCATEGORIES.get(i % SUBCATEGORIES.size()),
                            "Referencia " + sku)),
                    json(List.of(
                            new ProductSpec("Referencia", sku),
                            new ProductSpec("Categoría", String.valueOf(categoryId))))));
        }

        return new SeedDataset(List.copyOf(categories), List.copyOf(products));
    }

    private String json(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el JSON del seeder", ex);
        }
    }

    private static String padding(long value) {
        return String.format(Locale.ROOT, "%06d", value);
    }

    private static String truncate(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
