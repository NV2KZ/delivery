package microarch.delivery.adapters.out.kafka;

import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.order.event.OrderCompletedDomainEvent;
import microarch.delivery.core.domain.model.order.event.OrderCreatedDomainEvent;
import microarch.delivery.core.ports.OrderEventsProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import queues.order.events.OrderEventsProto;

@Component
@RequiredArgsConstructor
public class OrderEventsProducerImpl implements OrderEventsProducer {
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    @Value("${app.kafka.orders-events-topic}")
    private String topic;

    @Override
    public void publish(OrderCreatedDomainEvent event) {
        var integrationEvent = OrderEventsProto.OrderCreatedIntegrationEvent
                .newBuilder()
                .setOrderId(event.getOrderId().toString())
                .build();
        kafkaTemplate.send(topic, event.getOrderId().toString(), integrationEvent.toByteArray());
    }

    @Override
    public void publish(OrderCompletedDomainEvent event) {
        var integrationEvent = OrderEventsProto.OrderCompletedIntegrationEvent.newBuilder()
                .setOrderId(event.getOrderId().toString())
                .setCourierId(event.getCourierId().toString())
                .build();
        kafkaTemplate.send(topic, event.getOrderId().toString(), integrationEvent.toByteArray());
    }
}
