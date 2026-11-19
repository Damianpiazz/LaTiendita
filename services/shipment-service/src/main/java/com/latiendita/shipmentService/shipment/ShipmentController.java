package com.latiendita.shipmentService.shipment;

import com.latiendita.shipmentService.address.DeliveryAddressService;
import com.latiendita.shipmentService.address.dto.AddressResponse;
import com.latiendita.shipmentService.address.dto.CreateAddressRequest;
import com.latiendita.shipmentService.address.dto.UpdateAddressRequest;
import com.latiendita.shipmentService.shipment.dto.CreateShipmentRequest;
import com.latiendita.shipmentService.shipment.dto.QuoteRequest;
import com.latiendita.shipmentService.shipment.dto.QuoteResponse;
import com.latiendita.shipmentService.shipment.dto.ShipmentResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final DeliveryAddressService addressService;
    private final QuoteService quoteService;

    @PostMapping("/quote")
    public QuoteResponse quote(@Valid @RequestBody QuoteRequest request) {
        return quoteService.quote(request);
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody CreateAddressRequest request) {
        AddressResponse created = addressService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/addresses")
    public List<AddressResponse> listAddresses() {
        return addressService.list();
    }

    @PutMapping("/addresses/{id}")
    public AddressResponse updateAddress(@PathVariable Long id,
                                         @Valid @RequestBody UpdateAddressRequest request) {
        return addressService.update(id, request);
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResponse created = shipmentService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/pending-dispatch")
    public Map<String, Long> countPendingDispatch() {
        return Map.of("count", shipmentService.countPendingDispatch());
    }

    @GetMapping("/order/{orderId}")
    public ShipmentResponse findByOrderId(@PathVariable Long orderId) {
        return shipmentService.findByOrderId(orderId);
    }

    @GetMapping("/{id}")
    public ShipmentResponse findById(@PathVariable Long id) {
        return shipmentService.findById(id);
    }

    @GetMapping
    public Page<ShipmentResponse> list(@RequestParam(required = false) ShipmentStatus status,
                                       Pageable pageable) {
        return status == null
                ? shipmentService.list(pageable)
                : shipmentService.listByStatus(status, pageable);
    }

    @PostMapping("/{id}/dispatch")
    public ShipmentResponse dispatch(@PathVariable Long id) {
        return shipmentService.dispatch(id);
    }

    @PostMapping("/{id}/deliver")
    public ShipmentResponse deliver(@PathVariable Long id) {
        return shipmentService.markDelivered(id);
    }
}
