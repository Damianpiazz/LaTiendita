package com.latiendita.customerService.newsletter;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public subscription entry point; no customer profile or JWT is required. */
@RestController
@RequestMapping("/api/customers/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterSubscriptionService service;

    @PostMapping("/subscriptions")
    public NewsletterSubscriptionResponse subscribe(
            @Valid @RequestBody NewsletterSubscriptionRequest request) {
        return service.subscribe(request.email());
    }
}
