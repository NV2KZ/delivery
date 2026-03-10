package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.CourierDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetAllCouriersQueryHandlerImpl implements GetAllCouriersQueryHandler {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Result<GetAllCouriersResponse, Error> handle(GetAllCouriersQuery query) {
        String jpql = """
                SELECT NEW microarch.delivery.core.application.queries.dto.CourierDto(
                    c.id, c.name, c.location
                )
                FROM Courier c
                """;

        List<CourierDto> courierDtos = em.createQuery(jpql, CourierDto.class)
                .getResultList();

        GetAllCouriersResponse response = new GetAllCouriersResponse(courierDtos);

        return Result.success(response);
    }
}
