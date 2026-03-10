package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Guard;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Speed;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateCourierCommand {
    private final String name;
    private final Speed speed;

    public static Result<CreateCourierCommand, Error> create(String name, int speed) {
        var err = Guard.againstNullOrEmpty(name, "name");
        if (err != null)
            return Result.failure(err);

        var speedResult = Speed.create(speed);
        if (speedResult.isFailure())
            return Result.failure(speedResult.getError());

        return Result.success(new CreateCourierCommand(name, speedResult.getValue()));
    }
}
