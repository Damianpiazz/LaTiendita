package com.latiendita.reviewService.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OutboxRelayTest {

    @Mock
    private OutboxEventRepository repository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(relay, "batchSize", 50);
        ReflectionTestUtils.setField(relay, "maxAttempts", 10);
    }

    @Test
    void shouldPublishPendingEventKeyedByAggregateIdAndMarkItSent() {
        OutboxEvent event = pendingEvent();
        when(repository.findNextBatch(50, 10)).thenReturn(List.of(event));
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        relay.relay();

        verify(kafkaTemplate).send("review-events", event.getAggregateId(), "{\"id\":\"1\"}");
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(event.getSentAt()).isNotNull();
        assertThat(event.getLastError()).isNull();
    }

    @Test
    void shouldMarkEventFailedWhenBrokerRejectsTheSend() {
        OutboxEvent event = pendingEvent();
        when(repository.findNextBatch(50, 10)).thenReturn(List.of(event));
        CompletableFuture<SendResult<String, String>> failed = new CompletableFuture<>();
        failed.completeExceptionally(new RuntimeException("broker down"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(failed);

        relay.relay();

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getAttempts()).isEqualTo(1);
        assertThat(event.getLastError()).contains("broker down");
    }

    @Test
    void shouldDoNothingWhenThereIsNoPendingBatch() {
        when(repository.findNextBatch(50, 10)).thenReturn(List.of());

        relay.relay();

        verifyNoInteractions(kafkaTemplate);
    }

    private OutboxEvent pendingEvent() {
        return OutboxEvent.create("review-events", UUID.randomUUID().toString(),
                "REVIEW_APPROVED", "{\"id\":\"1\"}");
    }
}
