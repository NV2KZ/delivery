package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.order.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    Optional<Order> findAnyCreated();

    List<Order> findAllAssigned();
}
