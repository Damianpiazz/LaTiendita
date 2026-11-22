package com.latiendita.productService.product.dto;

import java.util.List;

/** RFC 4180 CSV rendering for the admin export. Every field is quoted. */
public final class AdminProductCsvWriter {

    public static final String HEADER =
            "productId,name,sku,category,price,stock,status,published";

    private static final String LINE_SEPARATOR = "\r\n";

    private AdminProductCsvWriter() {}

    public static String toCsv(List<AdminProductResponse> products) {
        StringBuilder csv = new StringBuilder(HEADER).append(LINE_SEPARATOR);
        for (AdminProductResponse product : products) {
            csv.append(field(product.productId())).append(',')
                    .append(field(product.name())).append(',')
                    .append(field(product.sku())).append(',')
                    .append(field(product.category())).append(',')
                    .append(field(product.price())).append(',')
                    .append(field(product.stock())).append(',')
                    .append(field(product.status())).append(',')
                    .append(field(product.published()))
                    .append(LINE_SEPARATOR);
        }
        return csv.toString();
    }

    private static String field(Object value) {
        String text = value == null ? "" : value.toString();
        return '"' + text.replace("\"", "\"\"") + '"';
    }
}
