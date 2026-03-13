package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.GeoClient;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateOrderCommandHandlerTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final GeoClient geoClient = mock(GeoClient.class);

    @Test
    void handleShouldCreateNewOrderWhenOrderDoesNotExist() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        var commandResult = CreateOrderCommand.create(
                orderId,
                "Россия",
                "Москва",
                "Тверская",
                "10",
                "25",
                5
        );
        assertThat(commandResult.isSuccess()).isTrue();
        var command = commandResult.getValue();

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        when(geoClient.getLocation(command.getAddress())).thenReturn(Location.mustCreate(1,2));

        var handler = new CreateOrderCommandHandlerImpl(orderRepository, geoClient);

        // Act
        Result<UUID, Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(orderId);

        verify(geoClient).getLocation(command.getAddress());
        verify(orderRepository).save(any());
    }

    @Test
    void handleShouldReturnExistingOrderIdWhenOrderAlreadyExists() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        var commandResult = CreateOrderCommand.create(
                orderId,
                "Россия",
                "Москва",
                "Тверская",
                "10",
                "25",
                5
        );
        assertThat(commandResult.isSuccess()).isTrue();
        var command = commandResult.getValue();

        var existingOrder = Order.mustCreate(orderId, Location.mustCreate(3, 4), Volume.mustCreate(5));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));

        var handler = new CreateOrderCommandHandlerImpl(orderRepository, geoClient);

        // Act
        Result<UUID, Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(orderId);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void handleShouldReturnFailureWhenCommandIsInvalid() {
        // Arrange
        var commandResult = CreateOrderCommand.create(
                null, // orderId null
                "Россия",
                "Москва",
                "Тверская",
                "10",
                "25",
                5
        );

        // Assert
        assertThat(commandResult.isFailure()).isTrue();
    }

    @Test
    void handleShouldReturnFailureWhenVolumeIsInvalid() {
        // Arrange
        var commandResult = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Россия",
                "Москва",
                "Тверская",
                "10",
                "25",
                0 // невалидный объем
        );

        // Assert
        assertThat(commandResult.isFailure()).isTrue();
    }
}