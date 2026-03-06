package microarch.delivery.adapters.out.postgres;

import lombok.AllArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final OrderJpaRepository jpa;

    @Override
    public Order save(Order order) {
        return jpa.save(order);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return jpa.findById(orderId);
    }

    @Override
    public Optional<Order> findAnyCreated() {
        return jpa.findFirstByStatus(OrderStatus.CREATED);
    }

    @Override
    public List<Order> findAllAssigned() {
        return jpa.findAllByStatus(OrderStatus.ASSIGNED);
    }
}
