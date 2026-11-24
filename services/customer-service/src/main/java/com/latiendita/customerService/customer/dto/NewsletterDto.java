package com.latiendita.customerService.customer.dto;

import java.time.Instant;

public record NewsletterDto(boolean newsletterOptIn, Instant newsletterConsentAt) {
}
