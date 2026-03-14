package microarch.delivery.core.application.commands;

import libs.ddd.DomainEventPublisher;
import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompleteOrderCommandHandlerImpl implements CompleteOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Override
    @Transactional
    public UnitResult<Error> handle(CompleteOrderCommand command) {
        var orderResult = orderRepository.findById(command.getOrderId());

        if (orderResult.isEmpty()) {
            return UnitResult.failure(Errors.orderNotFound(command.getOrderId()));
        }

        var order = orderResult.get();

        if (!Objects.equals(command.getCourierId(), order.getCourierId())) {
            return UnitResult.failure(Errors.orderNotCorrespondToCourier(command.getOrderId(), command.getCourierId()));
        }

        var courierOpt = courierRepository.findById(order.getCourierId());

        if (courierOpt.isEmpty()) {
            return UnitResult.failure(Errors.courierNotFound(command.getCourierId()));
        }

        var courier = courierOpt.get();

        if (courier.isInTargetLocation(order.getLocation())) {
            var completeResult = order.complete();
            if (completeResult.isFailure()) {
                UnitResult.failure(completeResult.getError());
            }

            var removeResult = courier.completeOrder(order.getId());
            if (removeResult.isFailure()) {
                UnitResult.failure(removeResult.getError());
            }
            orderRepository.save(order);
            courierRepository.save(courier);
            domainEventPublisher.publish(List.of(order));
            return UnitResult.success();
        } else {
            return UnitResult.failure(Errors.courierNotInTargetLocation(courier.getId()));
        }
    }

    private static class Errors {
        public static Error courierNotFound(UUID courierId) {
            return Error.of(
                    "courier.not.found",
                    String.format("Курьер с id %s не найден", courierId)
            );
        }

        public static Error courierNotInTargetLocation(UUID courierId) {
            return Error.of(
                    "courier.not.in.target.location",
                    String.format("Курьер с id %s не находится в месте назначения", courierId)
            );
        }

        public static Error orderNotCorrespondToCourier(UUID orderId, UUID courierId) {
            return Error.of(
                    "order.not.correspond.to.courier",
                    String.format("Заказ с id %s не доставляется курьером с id %s", orderId, courierId)
            );
        }

        public static Error orderNotFound(UUID orderId) {
            return Error.of(
                    "order.not.found",
                    String.format("Заказ с id %s не найден", orderId)
            );
        }
    }
}
