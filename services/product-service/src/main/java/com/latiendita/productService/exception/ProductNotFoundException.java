package com.latiendita.productService.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("Producto no encontrado con id " + id);
    }

    public ProductNotFoundException(String slug) {
        super("Producto no encontrado con slug " + slug);
    }
}
