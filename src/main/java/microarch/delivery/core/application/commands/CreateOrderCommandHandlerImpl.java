package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CreateOrderCommandHandlerImpl implements CreateOrderCommandHandler {

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateOrderCommand command) {
        var existingOrder = orderRepository.findById(command.getOrderId());
        if (existingOrder.isEmpty()) {
            var orderCreateResult = Order.create(command.getOrderId(), randomLocation(), command.getVolume());
            if (orderCreateResult.isFailure())
                return Result.failure(orderCreateResult.getError());
            var order = orderCreateResult.getValue();

            orderRepository.save(order);

            return Result.success(order.getId());
        }
        // Если заказ уже есть, просто возвращаем его id
        return Result.success(existingOrder.get().getId());
    }

    // TODO В следующих уроках мы будем передавать Address в сервис Geo и получать Location.
    //  Но пока у нас нет этой интеграции - используйте рандомную Location для создания заказа.
    private Location randomLocation() {
        int x = ThreadLocalRandom.current().nextInt(1, 11);
        int y = ThreadLocalRandom.current().nextInt(1, 11);
        return Location.mustCreate(x, y);
    }
}
