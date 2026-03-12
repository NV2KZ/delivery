package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;


import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MoveCourierInLocationCommand {
    private final UUID courierId;
    private final Location location;

    public static Result<MoveCourierInLocationCommand, Error> create(UUID courierId, Location location) {
        Objects.requireNonNull(courierId);
        Objects.requireNonNull(location);
        return Result.success(new MoveCourierInLocationCommand(courierId, location));
    }

}
