package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Speed;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.services.OrderDispatcher;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AssignOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final OrderDispatcher orderDispatcher = mock(OrderDispatcher.class);

    @Test
    void handleShouldBeSuccessWhenOrderAssignedToCourier() {
        // Arrange
        var command = AssignOrderCommand.create().getValue();
        var order = createOrder();
        var courier = createCourier();

        when(orderRepository.findAnyCreated()).thenReturn(Optional.of(order));
        when(courierRepository.findAllAvailable()).thenReturn(List.of(courier));

        Result<Courier, Error> dispatchResult = Result.success(courier);
        when(orderDispatcher.dispatch(order, List.of(courier))).thenReturn(dispatchResult);

        var handler = new AssignOrderCommandHandlerImpl(orderRepository, courierRepository, orderDispatcher);

        // Act
        UnitResult<Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        verify(orderDispatcher).dispatch(order, List.of(courier));
        verify(orderRepository).save(order);
        verify(courierRepository).save(courier);
    }

    @Test
    void handleShouldBeSuccessWhenNoCreatedOrders() {
        // Arrange
        var command = AssignOrderCommand.create().getValue();

        when(orderRepository.findAnyCreated()).thenReturn(Optional.empty());

        var handler = new AssignOrderCommandHandlerImpl(orderRepository, courierRepository, orderDispatcher);

        // Act
        UnitResult<Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        verify(orderRepository, never()).save(any());
        verify(courierRepository, never()).save(any());
        verify(orderDispatcher, never()).dispatch(any(), any());
    }

    @Test
    void handleShouldReturnFailureWhenNoAvailableCouriers() {
        // Arrange
        var command = AssignOrderCommand.create().getValue();
        var order = createOrder();

        when(orderRepository.findAnyCreated()).thenReturn(Optional.of(order));
        when(courierRepository.findAllAvailable()).thenReturn(List.of());

        Result<Courier, Error> dispatchResult = Result.failure(
                Error.of("courier.is.not.found.order", "Нет доступных курьеров")
        );
        when(orderDispatcher.dispatch(order, List.of())).thenReturn(dispatchResult);

        var handler = new AssignOrderCommandHandlerImpl(orderRepository, courierRepository, orderDispatcher);

        // Act
        UnitResult<Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("courier.is.not.found.order");
        verify(orderRepository, never()).save(any());
        verify(courierRepository, never()).save(any());
    }

    // Вспомогательные методы
    private Order createOrder() {
        return Order.mustCreate(
                UUID.randomUUID(),
                Location.mustCreate(5, 5),
                Volume.mustCreate(10)
        );
    }

    private Courier createCourier() {
        return Courier.mustCreate(
                "Иван Петров",
                Speed.mustCreate(2),
                Location.mustCreate(1, 1)
        );
    }
}