package com.latiendita.productService.product;

import com.latiendita.productService.storage.ImageStorageService;
import com.latiendita.productService.storage.ImageStorageService.StoredImage;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * GET /api/products/{id}/image — sirve la imagen del producto.
 *
 * <p>Misma URL relativa en local y producción: el gateway ya enruta
 * /api/products/** hacia este servicio, así que el navegador nunca habla
 * directo con Azurite ni con Azure. Si {@code image_url} es una URL externa
 * (http/...) redirige; si es una clave de blob, proxya los bytes con cache.
 */
@RestController
@RequiredArgsConstructor
public class ProductImageController {

    /** Respuesta del upload: clave en storage y URL relativa servida por el gateway. */
    public record AdminImageUpload(String key, String url) {}

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final String KEY_PREFIX = "productos/";

    private final ProductService productService;
    private final ImageStorageService images;

    /**
     * Sube una imagen del admin y devuelve la clave y la URL relativa que el
     * formulario guarda en `image_url`/`images`. El gateway ya enruta
     * `/api/products/**`, así que la URL sirve en local y en producción.
     */
    @PostMapping(value = "/api/products/admin/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminImageUpload upload(@RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen está vacío");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("La imagen no puede superar los 5 MB");
        }

        String key = images.newKey(file.getOriginalFilename());
        try (var input = file.getInputStream()) {
            images.upload(key, input, contentType);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("No se pudo leer la imagen subida", ex);
        }

        return new AdminImageUpload(key, "/api/products/images/" + key);
    }

    /**
     * Sirve cualquier imagen del catálogo por su clave, no solo la principal.
     * El prefijo y la ausencia de `..` evitan escapar del contenedor de blobs.
     */
    @GetMapping("/api/products/images/{*key}")
    public ResponseEntity<byte[]> imageByKey(@PathVariable String key) {
        String normalized = key.startsWith("/") ? key.substring(1) : key;
        if (!normalized.startsWith(KEY_PREFIX) || normalized.contains("..")) {
            return ResponseEntity.badRequest().build();
        }
        Optional<StoredImage> stored = images.download(normalized);
        return stored
                .map(s -> ResponseEntity.ok()
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                        .header(HttpHeaders.CONTENT_TYPE, s.contentType() != null
                                ? s.contentType()
                                : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .body(s.content()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/products/{productId}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long productId) {
        String ref = productService.findById(productId).imageUrl();
        if (ref == null || ref.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        if (ref.startsWith("http://") || ref.startsWith("https://")) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(ref))
                    .build();
        }
        Optional<StoredImage> stored = images.download(ref);
        return stored
                .map(s -> ResponseEntity.ok()
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                        .header(HttpHeaders.CONTENT_TYPE, s.contentType() != null
                                ? s.contentType()
                                : "application/octet-stream")
                        .body(s.content()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
