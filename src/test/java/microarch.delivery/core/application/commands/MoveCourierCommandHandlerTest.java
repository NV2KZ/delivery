package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Speed;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MoveCourierCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);

    @Test
    void handleShouldMoveAndSaveCouriersWhenAssignedOrdersExist() {
        // Arrange

        var courier1 = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(1, 1));
        var courier2 = Courier.mustCreate("Петр", Speed.mustCreate(2), Location.mustCreate(2, 2));

        var order1 = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(5, 5), Volume.mustCreate(10));
        var order2 = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(8, 8), Volume.mustCreate(10));

        // Курьеры берут заказы
        courier1.takeOrder(order1.getId(), Volume.mustCreate(10));
        courier2.takeOrder(order2.getId(), Volume.mustCreate(10));

        // Заказы назначаются на курьеров
        order1.assign(courier1.getId());
        order2.assign(courier2.getId());

        when(orderRepository.findAllAssigned()).thenReturn(List.of(order1, order2));
        when(courierRepository.findById(courier1.getId())).thenReturn(Optional.of(courier1));
        when(courierRepository.findById(courier2.getId())).thenReturn(Optional.of(courier2));

        var handler = new MoveCourierCommandHandlerImpl(orderRepository, courierRepository);

        // Act
        UnitResult<Error> result = handler.handle();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        verify(courierRepository, times(2)).save(any(Courier.class));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void handleShouldCompleteOrdersAndSaveBothWhenCourierInTargetLocation() {
        // Arrange

        var targetLocation = Location.mustCreate(5, 5);
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), targetLocation);
        var order = Order.mustCreate(UUID.randomUUID(), targetLocation, Volume.mustCreate(10));

        // Курьер берет заказ
        courier.takeOrder(order.getId(), Volume.mustCreate(10));
        // Заказ назначается на курьера
        order.assign(courier.getId());

        when(orderRepository.findAllAssigned()).thenReturn(List.of(order));
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courier));

        var handler = new MoveCourierCommandHandlerImpl(orderRepository, courierRepository);

        // Act
        UnitResult<Error> result = handler.handle();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        verify(orderRepository).save(order);
        verify(courierRepository).save(courier);
    }

    @Test
    void handleShouldNotSaveWhenMoveFails() {
        // Arrange

        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(1, 1));
        var order = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(5, 5), Volume.mustCreate(10));

        courier.takeOrder(order.getId(), Volume.mustCreate(10));
        order.assign(courier.getId());

        var courierSpy = spy(courier);
        doReturn(UnitResult.failure(Error.of("move.error", "Ошибка")))
                .when(courierSpy).move(any());

        when(orderRepository.findAllAssigned()).thenReturn(List.of(order));
        when(courierRepository.findById(courier.getId())).thenReturn(Optional.of(courierSpy));

        var handler = new MoveCourierCommandHandlerImpl(orderRepository, courierRepository);

        // Act
        UnitResult<Error> result = handler.handle();

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("move.couriers.partial");
        verify(courierRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void handleShouldReturnSuccessWhenNoAssignedOrders() {
        // Arrange

        when(orderRepository.findAllAssigned()).thenReturn(List.of());

        var handler = new MoveCourierCommandHandlerImpl(orderRepository, courierRepository);

        // Act
        UnitResult<Error> result = handler.handle();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        verify(courierRepository, never()).findById(any());
        verify(courierRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void handleShouldCollectErrorsWhenCourierNotFound() {
        // Arrange

        var courierId = UUID.randomUUID();
        var order = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(5, 5), Volume.mustCreate(10));
        order.assign(courierId);

        when(orderRepository.findAllAssigned()).thenReturn(List.of(order));
        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        var handler = new MoveCourierCommandHandlerImpl(orderRepository, courierRepository);

        // Act
        UnitResult<Error> result = handler.handle();

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("move.couriers.partial");
        verify(courierRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }
}