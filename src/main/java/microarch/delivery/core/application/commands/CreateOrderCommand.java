package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateOrderCommand {
    private final UUID orderId;

    private final String country;

    private final String city;

    private final String street;

    private final String house;

    private final String apartment;

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
        var err = Guard.combine(
                Guard.againstNullOrEmpty(orderId, "orderId"),
                Guard.againstNullOrEmpty(country, "country"),
                Guard.againstNullOrEmpty(city, "city"),
                Guard.againstNullOrEmpty(street, "street"),
                Guard.againstNullOrEmpty(house, "house"),
                Guard.againstNullOrEmpty(apartment, "apartment"));
        if (err != null)
            return Result.failure(err);

        var volumeResult = Volume.create(volume);
        if (volumeResult.isFailure())
            return Result.failure(volumeResult.getError());

        return Result.success(
                new CreateOrderCommand(
                        orderId,
                        country,
                        city,
                        street,
                        house,
                        apartment,
                        volumeResult.getValue()
                )
        );
    }
}
