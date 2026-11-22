package com.latiendita.productService.product;

import com.latiendita.productService.product.dto.AdminLowStockResponse;
import com.latiendita.productService.product.dto.AdminProductResponse;
import com.latiendita.productService.product.dto.AdminProductSummaryResponse;
import com.latiendita.productService.product.dto.ProductDto;
import com.latiendita.productService.product.dto.ProductIdsRequest;
import com.latiendita.productService.product.dto.ProductStatusUpdateRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductDto dto) {
        ProductDto created = productService.create(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{productId}")
                .buildAndExpand(created.productId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping
    public ProductDto update(@Valid @RequestBody ProductDto dto) {
        return productService.update(dto.productId(), dto);
    }

    @PutMapping("/{productId}")
    public ProductDto update(@PathVariable Long productId,
                             @Valid @RequestBody ProductDto dto) {
        return productService.update(productId, dto);
    }

    /**
     * GET /api/products?q=&categoryId=&featured=&page=&size=&sort=
     *
     * Los filtros son opcionales y combinables: sin parámetros se comporta igual
     * que antes. `sort` sólo acepta `propiedad` o `propiedad,asc|desc` sobre
     * propiedades de la entidad; cualquier otro formato responde 400.
     */
    @GetMapping
    public Page<ProductDto> findAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean featured,
            Pageable pageable) {
        return productService.search(q, categoryId, featured, pageable);
    }

    @GetMapping("/admin/low-stock")
    public AdminLowStockResponse lowStock(
            @RequestParam(defaultValue = "5") int threshold,
            @RequestParam(defaultValue = "5") int size) {
        return productService.adminLowStock(threshold, size);
    }

    /** GET /api/products/admin/summary?q=&categoryId= */
    @GetMapping("/admin/summary")
    public AdminProductSummaryResponse adminSummary(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId) {
        return productService.adminSummary(q, categoryId);
    }

    /** GET /api/products/admin?q=&status=&categoryId=&page=&size=&sort= */
    @GetMapping("/admin")
    public Page<AdminProductResponse> adminProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {
        return productService.adminProducts(q, status, categoryId, pageable);
    }

    /** POST /api/products/admin/status — publica o archiva varios productos a la vez. */
    @PostMapping("/admin/status")
    public ResponseEntity<Void> updateStatus(
            @Valid @RequestBody ProductStatusUpdateRequest request) {
        productService.updatePublishedStatus(request.productIds(), request.published());
        return ResponseEntity.noContent().build();
    }

    /** DELETE /api/products/admin — borrado masivo con el ID en el body. */
    @DeleteMapping("/admin")
    public ResponseEntity<Void> deleteProducts(@Valid @RequestBody ProductIdsRequest request) {
        productService.deleteProducts(request.productIds());
        return ResponseEntity.noContent().build();
    }

    /** GET /api/products/admin/export.csv?q=&status=&categoryId=&sort= */
    @GetMapping("/admin/export.csv")
    public ResponseEntity<String> exportCsv(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {
        String csv = productService.exportAdminCsv(q, status, categoryId, pageable.getSort());
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header("Content-Disposition", "attachment; filename=\"productos-admin.csv\"")
                .body(csv);
    }

    /** Acepta tanto el ID numérico histórico como el slug del catálogo. */
    @GetMapping("/{productIdOrSlug}")
    public ProductDto findByIdOrSlug(@PathVariable String productIdOrSlug) {
        return productService.findByIdOrSlug(productIdOrSlug);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable Long productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }
}
