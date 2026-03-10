package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CreateCourierCommandHandlerTest {

    private final CourierRepository courierRepository = mock(CourierRepository.class);

    @Test
    void handleShouldBeSuccessWhenCommandIsValid() {
        // Arrange
        var commandResult = CreateCourierCommand.create("Иван Петров", 2);
        assertThat(commandResult.isSuccess()).isTrue();
        var command = commandResult.getValue();

        var handler = new CreateCourierCommandHandlerImpl(courierRepository);

        // Act
        Result<UUID, Error> result = handler.handle(command);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isNotNull();

        verify(courierRepository).save(any());
    }

    @Test
    void handleShouldReturnFailureWhenNameIsEmpty() {
        // Arrange
        var commandResult = CreateCourierCommand.create("", 2);
        assertThat(commandResult.isFailure()).isTrue();
    }

    @Test
    void handleShouldReturnFailureWhenSpeedIsInvalid() {
        // Arrange
        var commandResult = CreateCourierCommand.create("Иван Петров", 0);
        assertThat(commandResult.isFailure()).isTrue();
    }
}