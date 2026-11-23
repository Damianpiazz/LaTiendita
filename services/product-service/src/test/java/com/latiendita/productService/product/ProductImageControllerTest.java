package com.latiendita.productService.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.productService.storage.ImageStorageService;
import com.latiendita.productService.storage.ImageStorageService.StoredImage;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class ProductImageControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ImageStorageService images;

    @InjectMocks
    private ProductImageController controller;

    @Test
    void shouldUploadAnImageAndReturnTheGatewayRelativeUrl() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "foto.png", "image/png", new byte[] {1, 2, 3});
        when(images.newKey("foto.png")).thenReturn("productos/uuid.png");

        var response = controller.upload(file);

        assertThat(response.key()).isEqualTo("productos/uuid.png");
        assertThat(response.url()).isEqualTo("/api/products/images/productos/uuid.png");
        verify(images).upload(eq("productos/uuid.png"), any(InputStream.class), eq("image/png"));
    }

    @Test
    void shouldRejectEmptyFilesAndNonImages() {
        assertThatThrownBy(() -> controller.upload(new MockMultipartFile("file", new byte[0])))
                .isInstanceOf(IllegalArgumentException.class);

        MockMultipartFile text =
                new MockMultipartFile("file", "nota.txt", "text/plain", new byte[] {1});
        assertThatThrownBy(() -> controller.upload(text))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldServeStoredImagesByKeyAndRejectEscapingKeys() {
        when(images.download("productos/uuid.png"))
                .thenReturn(Optional.of(new StoredImage(new byte[] {1}, "image/png")));

        var response = controller.imageByKey("productos/uuid.png");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("image/png");
        assertThat(controller.imageByKey("secret.png").getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(controller.imageByKey("productos/../secret.png").getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
