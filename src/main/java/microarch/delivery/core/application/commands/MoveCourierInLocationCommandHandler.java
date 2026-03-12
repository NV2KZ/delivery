package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;

public interface MoveCourierInLocationCommandHandler {
    UnitResult<Error> handle(MoveCourierInLocationCommand command);
}
