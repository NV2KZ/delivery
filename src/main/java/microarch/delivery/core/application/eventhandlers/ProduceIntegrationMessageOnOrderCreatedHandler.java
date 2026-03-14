package microarch.delivery.core.application.eventhandlers;

import microarch.delivery.adapters.out.kafka.OrderEventsProducerImpl;
import microarch.delivery.core.domain.model.order.event.OrderCreatedDomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ProduceIntegrationMessageOnOrderCreatedHandler {
    private final OrderEventsProducerImpl producer;

    public ProduceIntegrationMessageOnOrderCreatedHandler(OrderEventsProducerImpl producer) {
        this.producer = producer;
    }

    @EventListener
    public void handle(OrderCreatedDomainEvent event) throws Exception {
        producer.publish(event);
    }
}
