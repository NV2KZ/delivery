package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Address;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateOrderCommand {
    private final UUID orderId;

    private final Address address;

    private final Volume volume;

    public static Result<CreateOrderCommand, Error> create(
            UUID orderId,
            String country,
            String city,
            String street,
            String house,
            String apartment,
            int volume
    ) {
        if (orderId == null)
            return Result.failure(GeneralErrors.valueIsRequired("name"));

        var addresResult = Address.create(country, city, street, house, apartment);
        if (addresResult.isFailure())
            return Result.failure(addresResult.getError());

        var volumeResult = Volume.create(volume);
        if (volumeResult.isFailure())
            return Result.failure(volumeResult.getError());

        return Result.success(
                new CreateOrderCommand(
                        orderId,
                        addresResult.getValue(),
                        volumeResult.getValue()
                )
        );
    }
}
