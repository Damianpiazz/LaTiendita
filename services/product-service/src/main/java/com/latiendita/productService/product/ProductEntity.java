package com.latiendita.productService.product;

import com.latiendita.productService.product.dto.JsonListConverter;
import com.latiendita.productService.product.dto.JsonSpecsConverter;
import com.latiendita.productService.product.dto.ProductSpec;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products", uniqueConstraints = {
        @UniqueConstraint(columnNames = "sku"),
        @UniqueConstraint(columnNames = "slug")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 255)
    @Column(length = 255)
    private String title;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Min(0)
    @Column
    private Integer quantity;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "slug", unique = true, length = 255)
    private String slug;

    @Column(length = 100)
    private String subcategory;

    @Builder.Default
    @Column(name = "featured")
    private Boolean featured = false;

    /**
     * Admin publish flag, independent from the Home-only `featured` flag. The
     * column default backfills existing rows when ddl-auto=update adds it; the
     * Java default covers newly constructed entities.
     */
    @Builder.Default
    @ColumnDefault("true")
    @Column(name = "published", nullable = false)
    private Boolean published = true;

    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(name = "original_price", precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(length = 100)
    private String fulfillment;

    @Builder.Default
    @Convert(converter = JsonListConverter.class)
    @Column(name = "images_json", columnDefinition = "text")
    private List<String> images = new ArrayList<>();

    @Builder.Default
    @Convert(converter = JsonListConverter.class)
    @Column(name = "description_json", columnDefinition = "text")
    private List<String> description = new ArrayList<>();

    @Builder.Default
    @Convert(converter = JsonListConverter.class)
    @Column(name = "highlights_json", columnDefinition = "text")
    private List<String> highlights = new ArrayList<>();

    @Builder.Default
    @Convert(converter = JsonSpecsConverter.class)
    @Column(name = "specs_json", columnDefinition = "text")
    private List<ProductSpec> specs = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
