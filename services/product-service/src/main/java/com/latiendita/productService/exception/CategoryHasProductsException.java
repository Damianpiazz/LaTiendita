package com.latiendita.productService.exception;

/**
 * Raised when a category still has products and the delete must be refused.
 * Products are never deleted or reassigned by this service.
 */
public class CategoryHasProductsException extends RuntimeException {

    public CategoryHasProductsException(String name, long productCount) {
        super("No se puede eliminar la categoría '" + name + "' porque tiene "
                + productCount + " productos asociados");
    }
}
