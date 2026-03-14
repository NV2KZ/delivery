package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.order.event.OrderCompletedDomainEvent;
import microarch.delivery.core.domain.model.order.event.OrderCreatedDomainEvent;

public interface OrderEventsProducer {
    void publish(OrderCreatedDomainEvent domainEvent);

    void publish(OrderCompletedDomainEvent domainEvent);
}
