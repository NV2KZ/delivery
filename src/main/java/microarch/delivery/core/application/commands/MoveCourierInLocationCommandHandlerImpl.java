package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoveCourierInLocationCommandHandlerImpl implements MoveCourierInLocationCommandHandler {
    private final CourierRepository courierRepository;

    @Override
    @Transactional
    public UnitResult<Error> handle(MoveCourierInLocationCommand command) {
        var courierOpt = courierRepository.findById(command.getCourierId());
        if (courierOpt.isEmpty())
            return UnitResult.failure(Errors.courierNotFound(command.getCourierId()));

        var courier = courierOpt.get();
        var moveResult = courier.move(command.getLocation());
        if (moveResult.isFailure())
            return UnitResult.failure(moveResult.getError());
        else
            courierRepository.save(courier);

        return UnitResult.success();
    }

    private static class Errors {
        public static Error courierNotFound(UUID courierId) {
            return Error.of(
                    "courier.not.found",
                    String.format("Курьер с id %s не найден", courierId)
            );
        }
    }
}
