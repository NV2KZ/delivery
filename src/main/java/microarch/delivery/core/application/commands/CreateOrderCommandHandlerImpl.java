package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClient;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private final OrderRepository orderRepository;

    private final GeoClient geoClient;

    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateOrderCommand command) {
        var existingOrder = orderRepository.findById(command.getOrderId());
        if (existingOrder.isEmpty()) {
            var orderLocation = geoClient.getLocation(command.getAddress());
            var orderCreateResult = Order.create(command.getOrderId(), orderLocation, command.getVolume());
            if (orderCreateResult.isFailure())
                return Result.failure(orderCreateResult.getError());
            var order = orderCreateResult.getValue();

            orderRepository.save(order);
            domainEventPublisher.publish(List.of(order));

            return Result.success(order.getId());
        }
        // Если заказ уже есть, просто возвращаем его id
        return Result.success(existingOrder.get().getId());
    }
}
