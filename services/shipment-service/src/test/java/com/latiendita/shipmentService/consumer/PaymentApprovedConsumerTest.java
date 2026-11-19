package com.latiendita.shipmentService.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.latiendita.shipmentService.consumer.event.PaymentApprovedEvent;
import com.latiendita.shipmentService.shipment.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class PaymentApprovedConsumerTest {

    private static final String APPROVED = """
            {"type":"PAYMENT_APPROVED","paymentId":99,"orderId":7,"orderNumber":"ord-7",\
            "customerId":"user-1","amount":2499.00,"currency":"ARS",\
            "provider":"MERCADO_PAGO","providerPaymentId":"prov-1",\
            "occurredAt":"2026-10-12T12:00:00Z"}
            """;

    @Mock
    private ShipmentService shipmentService;

    private PaymentApprovedConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PaymentApprovedConsumer(shipmentService, new ObjectMapper());
    }

    @Test
    void shouldDelegateApprovedPaymentWithParsedFields() {
        consumer.onPaymentEvent(APPROVED);

        ArgumentCaptor<PaymentApprovedEvent> captor = ArgumentCaptor.forClass(PaymentApprovedEvent.class);
        verify(shipmentService).approvePayment(captor.capture());
        PaymentApprovedEvent event = captor.getValue();
        assertThat(event.orderId()).isEqualTo(7L);
        assertThat(event.customerId()).isEqualTo("user-1");
        assertThat(event.paymentId()).isEqualTo(99L);
    }

    @Test
    void shouldIgnoreUnrelatedEventTypes() {
        consumer.onPaymentEvent("""
                {"type":"PAYMENT_REJECTED","orderId":7,"customerId":"user-1"}
                """);

        verifyNoInteractions(shipmentService);
    }

    @Test
    void shouldDiscardMalformedJsonWithoutFailing() {
        consumer.onPaymentEvent("esto no es json {{{");
        consumer.onPaymentEvent("");

        verifyNoInteractions(shipmentService);
    }
}
