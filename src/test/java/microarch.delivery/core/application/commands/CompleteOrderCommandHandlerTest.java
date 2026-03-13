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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CompleteOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final CompleteOrderCommandHandler handler =
            new CompleteOrderCommandHandlerImpl(orderRepository, courierRepository);

    @Test
    void handle_ShouldCompleteOrder_WhenCourierAtTargetLocation() {
        // Arrange
        var courierId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var targetLocation = Location.mustCreate(5, 5);

        var command = CompleteOrderCommand.create(courierId, orderId).getValue();

        var order = Order.mustCreate(orderId, targetLocation, Volume.mustCreate(10));
        order.assign(courierId);

        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), targetLocation);
        courier.takeOrder(orderId, Volume.mustCreate(10));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));

        // Act
        UnitResult<Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        verify(orderRepository).save(order);
        verify(courierRepository).save(courier);
    }

    @Test
    void handle_ShouldReturnFailure_WhenOrderNotFound() {
        // Arrange
        var courierId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var command = CompleteOrderCommand.create(courierId, orderId).getValue();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        UnitResult<Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("order.not.found");
        verify(orderRepository, never()).save(any());
        verify(courierRepository, never()).save(any());
    }

    @Test
    void handle_ShouldReturnFailure_WhenOrderNotAssignedToThisCourier() {
        // Arrange
        var courierId = UUID.randomUUID();
        var otherCourierId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var command = CompleteOrderCommand.create(courierId, orderId).getValue();

        var order = Order.mustCreate(orderId, Location.mustCreate(5, 5), Volume.mustCreate(10));
        order.assign(otherCourierId); // Заказ назначен на другого курьера

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        UnitResult<Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("order.not.correspond.to.courier");
        verify(orderRepository, never()).save(any());
        verify(courierRepository, never()).save(any());
    }
}