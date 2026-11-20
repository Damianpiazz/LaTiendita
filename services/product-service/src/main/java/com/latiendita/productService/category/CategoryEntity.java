package com.latiendita.productService.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Size(max = 500)
    @Column(length = 500)
    private String description;

    /**
     * Categoría destacada en la Home. El default en Java cubre las entidades
     * nuevas y el default de la columna cubre las filas ya existentes cuando
     * Hibernate aplica `ddl-auto=update` (único mecanismo de esquema del repo).
     */
    @Builder.Default
    @Column(name = "featured", nullable = false, columnDefinition = "boolean default false")
    private boolean featured = false;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
