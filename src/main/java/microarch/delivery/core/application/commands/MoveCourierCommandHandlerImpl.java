package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoveCourierCommandHandlerImpl implements MoveCourierCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;

    @Override
    @Transactional
    public UnitResult<Error> handle(MoveCourierCommand command) {
        var assignedOrders = orderRepository.findAllAssigned();

        if (assignedOrders.isEmpty()) {
            return UnitResult.success();
        }

        List<Error> errors = new ArrayList<>();

        for (var order : assignedOrders) {
            var courierOpt = courierRepository.findById(order.getCourierId());

            if (courierOpt.isEmpty()) {
                errors.add(Errors.courierNotFound(order.getCourierId(), order.getId()));
                continue;
            }

            var courier = courierOpt.get();

            if (courier.isInTargetLocation(order.getLocation())) {
                var completeResult = order.complete();
                if (completeResult.isFailure()) {
                    errors.add(completeResult.getError());
                    continue;
                }

                var removeResult = courier.completeOrder(order.getId());
                if (removeResult.isFailure()) {
                    errors.add(removeResult.getError());
                }
                orderRepository.save(order);
                courierRepository.save(courier);
                continue;
            }

            var moveResult = courier.move(order.getLocation());
            if (moveResult.isFailure()) {
                errors.add(moveResult.getError());
            } else {
                courierRepository.save(courier);
            }
        }

        return errors.isEmpty()
                ? UnitResult.success()
                : UnitResult.failure(Errors.moveCouriersPartialErrors(errors));
    }

    private static class Errors {
        public static Error moveCouriersPartialErrors(List<Error> errors) {
            return Error.of("move.couriers.partial",
                    String.format("Часть операций завершилась с ошибками. Количество ошибок: %d", errors.size()));
        }

        public static Error courierNotFound(UUID courierId, UUID orderId) {
            return Error.of(
                    "courier.not.found",
                    String.format("Курьер с id %s не найден для заказа %s", courierId, orderId)
            );
        }
    }
}
