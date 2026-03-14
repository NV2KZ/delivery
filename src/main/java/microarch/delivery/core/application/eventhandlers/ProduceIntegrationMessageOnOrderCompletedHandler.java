package microarch.delivery.core.application.eventhandlers;

import microarch.delivery.adapters.out.kafka.OrderEventsProducerImpl;
import microarch.delivery.core.domain.model.order.event.OrderCompletedDomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ProduceIntegrationMessageOnOrderCompletedHandler {
    private final OrderEventsProducerImpl producer;

    public ProduceIntegrationMessageOnOrderCompletedHandler(OrderEventsProducerImpl producer) {
        this.producer = producer;
    }

    @EventListener
    public void handle(OrderCompletedDomainEvent event) throws Exception {
        producer.publish(event);
    }
}
