package microarch.delivery.core.domain.services;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Speed;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDispatcherTest {

    private final OrderDispatcher dispatcher = new OrderDispatcherImpl();

    @Test
    void shouldAssignOrderToFastestCourier() {
        // Arrange
        var orderLocation = Location.mustCreate(5, 5);
        var order = Order.mustCreate(UUID.randomUUID(), orderLocation, Volume.mustCreate(5));

        var slowCourier = Courier.mustCreate("Медленный", Speed.mustCreate(1), Location.mustCreate(1, 1));
        var fastCourier = Courier.mustCreate("Быстрый", Speed.mustCreate(3), Location.mustCreate(1, 1));
        var couriers = List.of(slowCourier, fastCourier);

        // Act
        var result = dispatcher.dispatch(order, couriers);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getName()).isEqualTo("Быстрый");
        assertThat(fastCourier.hasNoActiveOrders()).isFalse();
        assertThat(slowCourier.hasNoActiveOrders()).isTrue();
    }

    @Test
    void shouldNotAssignOrderWhenNoSuitableCourier() {
        // Arrange
        var order = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(5, 5), Volume.mustCreate(20));
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(1, 1)); // только сумка на 10

        // Act
        var result = dispatcher.dispatch(order, List.of(courier));

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(courier.hasNoActiveOrders()).isTrue();
    }
}