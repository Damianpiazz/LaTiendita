package com.latiendita.customerService.consumer;

import com.latiendita.customerService.customer.CustomerEntity;
import com.latiendita.customerService.customer.CustomerRepository;
import com.latiendita.customerService.customer.CustomerStatus;
import com.latiendita.customerService.customer.event.UserRegisteredEvent;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredConsumer {

    public static final String USER_EVENTS_TOPIC = "user-events";
    public static final String USER_REGISTERED = "USER_REGISTERED";

    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = USER_EVENTS_TOPIC, groupId = "customer-service")
    @Transactional
    public void onUserEvent(String payload) {
        String type = readEventType(payload);
        if (!USER_REGISTERED.equals(type)) {
            return;
        }

        UserRegisteredEvent event;
        try {
            event = objectMapper.readValue(payload, UserRegisteredEvent.class);
        } catch (Exception ex) {
            log.warn("Malformed USER_REGISTERED event discarded: {}", payload);
            return;
        }

        createIfAbsent(event);
    }

    private String readEventType(String payload) {
        try {
            Map<?, ?> node = objectMapper.readValue(payload, Map.class);
            Object type = node.get("type");
            return type == null ? null : type.toString();
        } catch (Exception ex) {
            log.warn("Malformed message in user-events discarded: {}", payload);
            return null;
        }
    }

    private void createIfAbsent(UserRegisteredEvent event) {
        if (customerRepository.findByAuthorizerSubAndDeletedAtIsNull(event.sub()).isPresent()) {
            log.info("Profile for sub {} already exists; ignoring (idempotent)", event.sub());
            return;
        }

        Instant now = Instant.now();
        CustomerEntity customer = CustomerEntity.builder()
                .authorizerSub(event.sub())
                .email(event.email())
                .firstName(event.firstName())
                .lastName(event.lastName())
                .status(CustomerStatus.ACTIVE)
                .newsletterOptIn(false)
                .lastLoginAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
        customerRepository.save(customer);
        log.info("Customer profile created from USER_REGISTERED for sub {}", event.sub());
    }
}
