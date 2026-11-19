package com.latiendita.shipmentService.address;

import com.latiendita.shipmentService.CurrentUser;
import com.latiendita.shipmentService.address.dto.AddressResponse;
import com.latiendita.shipmentService.address.dto.CreateAddressRequest;
import com.latiendita.shipmentService.address.dto.UpdateAddressRequest;
import com.latiendita.shipmentService.exception.AddressAccessDeniedException;
import com.latiendita.shipmentService.exception.AddressNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryAddressService {

    private final DeliveryAddressRepository repository;

    @Transactional(readOnly = true)
    public List<AddressResponse> list() {
        return repository.findByCustomerIdOrderByIdDesc(CurrentUser.id()).stream()
                .map(DeliveryAddressService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeliveryAddressEntity findOwned(Long id) {
        DeliveryAddressEntity address = repository.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));
        if (!address.getCustomerId().equals(CurrentUser.id())) {
            throw new AddressAccessDeniedException(id);
        }
        return address;
    }

    @Transactional(readOnly = true)
    public Optional<DeliveryAddressEntity> findDefaultForCustomer(String customerId) {
        return repository.findByCustomerIdOrderByIdDesc(customerId).stream()
                .filter(DeliveryAddressEntity::isDefault)
                .findFirst();
    }

    @Transactional
    public AddressResponse create(CreateAddressRequest request) {
        Instant now = Instant.now();
        DeliveryAddressEntity address = DeliveryAddressEntity.builder()
                .customerId(CurrentUser.id())
                .recipientName(request.recipientName())
                .phone(request.phone())
                .addressLine(request.addressLine())
                .city(request.city())
                .province(request.province())
                .postalCode(request.postalCode())
                .references(request.references())
                .isDefault(request.isDefault())
                .createdAt(now)
                .updatedAt(now)
                .build();
        return toResponse(repository.save(address));
    }

    @Transactional
    public AddressResponse update(Long id, UpdateAddressRequest request) {
        DeliveryAddressEntity address = findOwned(id);
        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setAddressLine(request.addressLine());
        address.setCity(request.city());
        address.setProvince(request.province());
        address.setPostalCode(request.postalCode());
        address.setReferences(request.references());
        address.setDefault(request.isDefault());
        address.setUpdatedAt(Instant.now());
        return toResponse(repository.save(address));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findOwned(id));
    }

    private static AddressResponse toResponse(DeliveryAddressEntity address) {
        return new AddressResponse(address.getId(), address.getRecipientName(), address.getPhone(),
                address.getAddressLine(), address.getCity(), address.getProvince(),
                address.getPostalCode(), address.getReferences(), address.isDefault());
    }
}
