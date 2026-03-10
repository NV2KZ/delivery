package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MoveCourierCommand {
    public static Result<MoveCourierCommand, Error> create() {
        return Result.success(new MoveCourierCommand());
    }
}
