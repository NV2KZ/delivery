package microarch.delivery.core.domain.services;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.order.Order;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class OrderDispatcherImpl implements OrderDispatcher {

    @Override
    public Result<Courier, Error> dispatch(Order order, List<Courier> couriers) {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(couriers, "couriers");
        if (order.isNotCreated()) {
            return Result.failure(Errors.orderIsNotCreated(order.getId()));
        }

        return couriers.stream()
                .filter(courier -> courier.canPlaceOrder(order.getVolume()))
                .filter(Courier::hasNoActiveOrders)
                .min(Comparator.comparing(courier -> courier.calculateDeliveryTime(order.getLocation()).getValueOrThrow()))
                .map(courier -> {
                    var result = courier.takeOrder(order.getId(), order.getVolume());
                    if (result.isSuccess()) {
                        return Result.success(courier);
                    } else {
                        return Result.<Courier, Error>failure(result.getError());
                    }
                })
                .orElse(Result.failure(Errors.courierIsNotFoundForOrder(order.getId())));
    }


    private static class Errors {
        public static Error orderIsNotCreated(UUID orderId) {
            return Error.of("order.is.not.created",
                    String.format("Заказ %s не находится в статусе CREATED", orderId));
        }

        public static Error courierIsNotFoundForOrder(UUID orderId) {
            return Error.of("courier.is.not.found.order",
                    String.format("Не найдено курьера для доставки заказа %s", orderId));
        }
    }
}
