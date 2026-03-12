package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;

public interface GetAllNotCompletedOrdersQueryHandler {
    Result<GetAllNotCompletedOrdersResponse, Error> handle();
}
