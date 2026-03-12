package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateCourierCommandHandlerImpl implements CreateCourierCommandHandler {

    private static final Location DEFAULT_LOCATION = Location.mustCreate(1,1);

    private final CourierRepository courierRepository;

    @Override
    @Transactional
    public Result<UUID, Error> handle(CreateCourierCommand command) {
        var courierCreateResult = Courier.create(command.getName(), command.getSpeed(), DEFAULT_LOCATION);

        if (courierCreateResult.isFailure())
            return Result.failure(courierCreateResult.getError());
        var courier = courierCreateResult.getValue();

        courierRepository.save(courier);

        return Result.success(courier.getId());
    }
}
