package com.latiendita.productService.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id) {
        super("Categoría no encontrada con id " + id);
    }

    public CategoryNotFoundException(String slug) {
        super("Categoría no encontrada con slug " + slug);
    }
}
