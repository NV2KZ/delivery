package microarch.delivery.core.domain.model.order.event;

import libs.ddd.DomainEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;

import java.util.UUID;

@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@Getter
public class OrderCreatedDomainEvent extends DomainEvent {
    private final UUID orderId;

    public OrderCreatedDomainEvent(Order order) {
        super(order);
        this.orderId = order.getId();
    }
}
