package microarch.delivery.core.application.queries;

import libs.errs.Error;
import libs.errs.Result;

public interface GetAllCouriersQueryHandler {
    Result<GetAllCouriersResponse, Error> handle(GetAllCouriersQuery query);
}
