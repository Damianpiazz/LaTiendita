package com.latiendita.productService.exception;

public class DuplicateSlugException extends RuntimeException {

    public DuplicateSlugException(String slug) {
        super("El slug '" + slug + "' ya está en uso");
    }
}
