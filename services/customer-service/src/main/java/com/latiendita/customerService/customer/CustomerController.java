package com.latiendita.customerService.customer;

import com.latiendita.customerService.customer.dto.CreateCustomerRequest;
import com.latiendita.customerService.customer.dto.CustomerDto;
import com.latiendita.customerService.customer.dto.CustomerSummaryDto;
import com.latiendita.customerService.customer.dto.NewsletterDto;
import com.latiendita.customerService.customer.dto.NewsletterRequest;
import com.latiendita.customerService.customer.dto.UpdateCustomerRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDto> create(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerCreation result = customerService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.customer().id())
                .toUri();
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .location(location)
                .body(result.customer());
    }

    @GetMapping("/me")
    public CustomerDto findMe() {
        return customerService.findMe();
    }

    @PatchMapping("/me")
    public CustomerDto updateMe(@Valid @RequestBody UpdateCustomerRequest request) {
        return customerService.updateMe(request);
    }

    @PutMapping("/me/newsletter")
    public NewsletterDto updateNewsletter(@Valid @RequestBody NewsletterRequest request) {
        return customerService.updateNewsletter(request);
    }

    /** Admin listing with optional, combinable name / email / status filters. */
    @GetMapping
    public Page<CustomerSummaryDto> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) CustomerStatus status,
            Pageable pageable) {
        return customerService.search(name, email, status, pageable);
    }

    @GetMapping("/{id}")
    public CustomerDto findById(@PathVariable UUID id) {
        return customerService.findById(id);
    }
}
