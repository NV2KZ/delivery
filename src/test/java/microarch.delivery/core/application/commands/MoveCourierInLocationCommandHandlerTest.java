package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Speed;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MoveCourierInLocationCommandHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);
    private final MoveCourierInLocationCommandHandler handler = new MoveCourierInLocationCommandHandlerImpl(courierRepository);

    @Test
    void handle_ShouldMoveCourier_WhenCourierExists() {
        // Arrange
        var courierId = UUID.randomUUID();
        var targetLocation = Location.mustCreate(5, 5);
        var command = MoveCourierInLocationCommand.create(courierId, targetLocation).getValue();

        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(1, 1));

        when(courierRepository.findById(courierId)).thenReturn(Optional.of(courier));

        // Act
        UnitResult<Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        verify(courierRepository).save(courier);
        assertThat(courier.getLocation()).isNotEqualTo(Location.mustCreate(1, 1));
    }

    @Test
    void handle_ShouldReturnFailure_WhenCourierNotFound() {
        // Arrange
        var courierId = UUID.randomUUID();
        var targetLocation = Location.mustCreate(5, 5);
        var command = MoveCourierInLocationCommand.create(courierId, targetLocation).getValue();

        when(courierRepository.findById(courierId)).thenReturn(Optional.empty());

        // Act
        UnitResult<Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("courier.not.found");
        verify(courierRepository, never()).save(any());
    }
}