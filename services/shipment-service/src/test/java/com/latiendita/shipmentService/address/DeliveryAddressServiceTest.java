package com.latiendita.shipmentService.address;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.latiendita.shipmentService.CurrentUser;
import com.latiendita.shipmentService.address.dto.AddressResponse;
import com.latiendita.shipmentService.address.dto.CreateAddressRequest;
import com.latiendita.shipmentService.address.dto.UpdateAddressRequest;
import com.latiendita.shipmentService.exception.AddressAccessDeniedException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class DeliveryAddressServiceTest {

    private static final String USER = "user-1";

    @Mock
    private DeliveryAddressRepository repository;

    @InjectMocks
    private DeliveryAddressService service;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CurrentUser.HEADER, USER);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldCreateAddressOwnedByCurrentUser() {
        when(repository.save(any(DeliveryAddressEntity.class))).thenAnswer(invocation -> {
            DeliveryAddressEntity entity = invocation.getArgument(0);
            entity.setId(5L);
            return entity;
        });

        AddressResponse response = service.create(createRequest("Casa"));

        ArgumentCaptor<DeliveryAddressEntity> captor = ArgumentCaptor.forClass(DeliveryAddressEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCustomerId()).isEqualTo(USER);
        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.isDefault()).isTrue();
        assertThat(response.recipientName()).isEqualTo("Ana Diaz");
    }

    @Test
    void shouldListOnlyCurrentUsersAddresses() {
        when(repository.findByCustomerIdOrderByIdDesc(USER)).thenReturn(List.of(address(5L, USER)));

        List<AddressResponse> response = service.list();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).id()).isEqualTo(5L);
    }

    @Test
    void shouldDenyUpdatingAddressOfAnotherUser() {
        when(repository.findById(7L)).thenReturn(Optional.of(address(7L, "user-2")));

        assertThatThrownBy(() -> service.update(7L, updateRequest()))
                .isInstanceOf(AddressAccessDeniedException.class)
                .hasMessageContaining("7");

        verify(repository, never()).save(any());
    }

    @Test
    void shouldDenyDeletingAddressOfAnotherUser() {
        when(repository.findById(7L)).thenReturn(Optional.of(address(7L, "user-2")));

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(AddressAccessDeniedException.class);

        verify(repository, never()).delete(any(DeliveryAddressEntity.class));
    }

    @Test
    void shouldDenyReadingAddressOfAnotherUser() {
        when(repository.findById(7L)).thenReturn(Optional.of(address(7L, "user-2")));

        assertThatThrownBy(() -> service.findOwned(7L))
                .isInstanceOf(AddressAccessDeniedException.class);
    }

    private CreateAddressRequest createRequest(String label) {
        return new CreateAddressRequest("Ana Diaz", "555-1234", "Av. Principal 123",
                "Buenos Aires", "CABA", "C1000", "Piso 2", true);
    }

    private UpdateAddressRequest updateRequest() {
        return new UpdateAddressRequest("Ana Diaz", "555-9999", "Av. Nueva 456",
                "Buenos Aires", "CABA", "C1000", null, false);
    }

    static DeliveryAddressEntity address(Long id, String customerId) {
        return DeliveryAddressEntity.builder()
                .id(id)
                .customerId(customerId)
                .recipientName("Ana Diaz")
                .phone("555-1234")
                .addressLine("Av. Principal 123")
                .city("Buenos Aires")
                .province("CABA")
                .postalCode("C1000")
                .isDefault(true)
                .build();
    }
}
