package com.latiendita.paymentService.payment;

import com.latiendita.paymentService.payment.dto.AdminPaymentResponse;
import com.latiendita.paymentService.payment.dto.AdminPaymentSummaryResponse;
import com.latiendita.paymentService.payment.dto.CreatePaymentRequest;
import com.latiendita.paymentService.payment.dto.PaymentResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse created = paymentService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{paymentId}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/admin/summary")
    public AdminPaymentSummaryResponse adminSummary(
            @RequestParam(required = false) Integer days) {
        return paymentService.summaryForAdmin(days);
    }

    @GetMapping("/admin")
    public Page<AdminPaymentResponse> adminList(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) Integer days,
            Pageable pageable) {
        return paymentService.findAdminPayments(q, status, days, pageable);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse findById(@PathVariable Long paymentId) {
        return paymentService.findById(paymentId);
    }

    @GetMapping
    public Page<PaymentResponse> findByCustomer(Pageable pageable) {
        return paymentService.findByCustomer(pageable);
    }

    @GetMapping("/order/{orderId}")
    public PaymentResponse findByOrderId(@PathVariable Long orderId) {
        return paymentService.findByOrderId(orderId);
    }

    /**
     * GET /api/payments/reference/{externalReference}
     *
     * Lo usan las páginas de retorno del checkout: Mercado Pago redirige con
     * ?external_reference=<orderNumber> y este endpoint lo traduce al pago.
     */
    @GetMapping("/reference/{externalReference}")
    public PaymentResponse findByExternalReference(
            @PathVariable String externalReference) {
        return paymentService.findByExternalReference(externalReference);
    }
}
