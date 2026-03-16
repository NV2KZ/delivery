package microarch.delivery.adapters.out.postgres.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import libs.ddd.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Job {
    private final ApplicationEventPublisher publisher;
    private final OutboxJpaRepository jpa;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 1000)
    public void run() {
        var outboxMessages = jpa.findUnprocessedMessages();
        for (var outboxMessage : outboxMessages) {
            try {
                // Динамически находим класс события
                var eventClassName = outboxMessage.getEventType();
                var eventClass = Class.forName(eventClassName);
                var eventObject = objectMapper.readValue(outboxMessage.getPayload(), eventClass);

                // Проверяем, что это DomainEvent
                if (!(eventObject instanceof DomainEvent domainEvent)) {
                    throw new IllegalStateException("Invalid outbox message type: " + eventClass);
                }

                // Публикуем доменное событие
                publisher.publishEvent(domainEvent);

                // Отмечаем как отправленное
                outboxMessage.markAsProcessed();
                jpa.save(outboxMessage);
            } catch (Exception e) {
                System.err.println("Failed to publish outbox message: " + e.getMessage());
            }
        }
    }
}