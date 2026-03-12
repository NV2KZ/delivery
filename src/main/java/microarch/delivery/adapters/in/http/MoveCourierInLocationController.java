package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.MoveCourierApi;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.commands.MoveCourierInLocationCommand;
import microarch.delivery.core.application.commands.MoveCourierInLocationCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MoveCourierInLocationController implements MoveCourierApi {

    private final MoveCourierInLocationCommandHandler handler;

    @Override
    public ResponseEntity<Void> moveCourier(
            UUID courierId,
            Location location
    ) {
        // Формируем команду
        var targetLocationResult = microarch.delivery.core.domain.model.kernel.Location.create(location.getX(), location.getY());
        if (targetLocationResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }

        var commandResult = MoveCourierInLocationCommand.create(
                courierId,
                targetLocationResult.getValue()
        );

        if (commandResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }
        var command = commandResult.getValue();

        // Обрабатываем команду
        var handleResult = handler.handle(command);
        if (handleResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
