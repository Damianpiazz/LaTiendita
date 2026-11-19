package com.latiendita.offerService.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class OutboxWriter {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public void append(String topic, String aggregateId, String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            repository.save(OutboxEvent.create(topic, aggregateId, eventType, json));
        } catch (JacksonException ex) {
            throw new IllegalStateException("Could not serialize event " + eventType, ex);
        }
    }
}
