package com.latiendita.shipmentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.shipmentService.address.DeliveryAddressEntity;
import com.latiendita.shipmentService.address.DeliveryAddressRepository;
import com.latiendita.shipmentService.order.OrderClient;
import com.latiendita.shipmentService.outbox.OutboxEvent;
import com.latiendita.shipmentService.outbox.OutboxEventRepository;
import com.latiendita.shipmentService.outbox.OutboxStatus;
import com.latiendita.shipmentService.shipment.ShipmentEntity;
import com.latiendita.shipmentService.shipment.ShipmentRepository;
import com.latiendita.shipmentService.shipment.ShipmentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "outbox.relay.poll-interval-ms=600000",
        "spring.kafka.listener.auto-startup=false"
})
class ShipmentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private DeliveryAddressRepository addressRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @MockitoBean
    private OrderClient orderClient;

    @BeforeEach
    void setup() {
        outboxEventRepository.deleteAll();
        shipmentRepository.deleteAll();
        addressRepository.deleteAll();
        lenient().when(orderClient.findById(7L))
                .thenReturn(new OrderClient.OrderDto(7L, "PENDING"));
    }

    @Test
    void shouldQuoteFlatRateAndBusinessDayWindow() throws Exception {
        mockMvc.perform(post("/api/shipments/quote")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subtotal\":1000.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingCost").value(2499))
                .andExpect(jsonPath("$.currency").value("ARS"))
                .andExpect(jsonPath("$.estimatedFrom").isNotEmpty())
                .andExpect(jsonPath("$.estimatedTo").isNotEmpty());
    }

    @Test
    void shouldRejectNegativeSubtotalWith400() throws Exception {
        mockMvc.perform(post("/api/shipments/quote")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subtotal\":-1.00}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.errors[0]").value(
                        containsString("subtotal debe ser mayor o igual a 0")));
    }

    @Test
    void shouldRejectMissingUserIdHeaderWith401() throws Exception {
        mockMvc.perform(post("/api/shipments/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"subtotal\":1000.00}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("No autorizado"));
    }

    @Test
    void shouldCreateAddressForCurrentUser() throws Exception {
        mockMvc.perform(post("/api/shipments/addresses")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientName":"Ana Diaz","phone":"555-1234",\
                                "addressLine":"Av. Principal 123","city":"Buenos Aires",\
                                "province":"CABA","postalCode":"C1000","isDefault":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/shipments/addresses/")))
                .andExpect(jsonPath("$.recipientName").value("Ana Diaz"))
                .andExpect(jsonPath("$.isDefault").value(true));

        assertThat(addressRepository.findAll()).hasSize(1);
        assertThat(addressRepository.findAll().get(0).getCustomerId()).isEqualTo("1");
    }

    @Test
    void shouldDenyUpdatingAnotherUsersAddressWith403() throws Exception {
        DeliveryAddressEntity address = addressRepository.save(address("2"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/shipments/addresses/{id}", address.getId())
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientName":"Ana Diaz","phone":"555-1234",\
                                "addressLine":"Av. Nueva 456","city":"Buenos Aires",\
                                "province":"CABA","postalCode":"C1000","isDefault":false}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Acceso denegado"));
    }

    @Test
    void shouldReturn404WhenShipmentDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/shipments/{id}", 999999L).header("X-User-Id", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Envío no encontrado"));
    }

    @Test
    void shouldCreateShipmentFromSavedAddressWithoutPublishingEvent() throws Exception {
        DeliveryAddressEntity address = addressRepository.save(address("1"));

        mockMvc.perform(post("/api/shipments")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":7,"addressId":%d,"shippingCost":2499.00,\
                                "currency":"ARS","estimatedFrom":"2026-10-15",\
                                "estimatedTo":"2026-10-19"}
                                """.formatted(address.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/shipments/")))
                .andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"))
                .andExpect(jsonPath("$.addressLine").value("Av. Principal 123"));

        assertThat(shipmentRepository.findAll()).hasSize(1);
        assertThat(outboxEventRepository.findAll()).isEmpty();
    }

    @Test
    void shouldDispatchAndPersistExactOutboxPayload() throws Exception {
        ShipmentEntity shipment = shipmentRepository.save(shipment());

        mockMvc.perform(post("/api/shipments/{id}/dispatch", shipment.getId())
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DISPATCHED"))
                .andExpect(jsonPath("$.dispatchedAt").isNotEmpty());

        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertThat(events).hasSize(1);
        OutboxEvent event = events.get(0);
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getTopic()).isEqualTo("shipment-events");
        assertThat(event.getEventType()).isEqualTo("SHIPMENT_DISPATCHED");
        assertThat(event.getAggregateId()).isEqualTo("77");
        assertThat(event.getPayload()).contains("\"type\":\"SHIPMENT_DISPATCHED\"",
                "\"shipmentId\":" + shipment.getId(), "\"orderId\":77",
                "\"customerId\":\"1\"", "\"estimatedTo\":\"2026-10-19\"");
    }

    @Test
    void shouldDeliverAndPersistDeliveredEvent() throws Exception {
        ShipmentEntity shipment = shipment();
        shipment.setStatus(ShipmentStatus.DISPATCHED);
        shipment.setDispatchedAt(Instant.now());
        shipment = shipmentRepository.save(shipment);

        mockMvc.perform(post("/api/shipments/{id}/deliver", shipment.getId())
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.deliveredAt").isNotEmpty());

        List<OutboxEvent> events = outboxEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventType()).isEqualTo("SHIPMENT_DELIVERED");
        assertThat(events.get(0).getPayload()).contains("\"shipmentId\":" + shipment.getId(),
                "\"orderId\":77", "\"customerId\":\"1\"");
    }

    private DeliveryAddressEntity address(String customerId) {
        Instant now = Instant.now();
        return DeliveryAddressEntity.builder()
                .customerId(customerId)
                .recipientName("Ana Diaz")
                .phone("555-1234")
                .addressLine("Av. Principal 123")
                .city("Buenos Aires")
                .province("CABA")
                .postalCode("C1000")
                .isDefault(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private ShipmentEntity shipment() {
        Instant now = Instant.now();
        return ShipmentEntity.builder()
                .orderId(77L)
                .customerId("1")
                .status(ShipmentStatus.PENDING_DISPATCH)
                .shippingCost(new BigDecimal("2499"))
                .currency("ARS")
                .recipientName("Ana Diaz")
                .phone("555-1234")
                .addressLine("Av. Principal 123")
                .city("Buenos Aires")
                .province("CABA")
                .postalCode("C1000")
                .estimatedFrom(LocalDate.of(2026, 10, 15))
                .estimatedTo(LocalDate.of(2026, 10, 19))
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();
    }
}
