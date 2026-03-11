package microarch.delivery.core.application.queries;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import libs.errs.Error;
import libs.errs.Result;
import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.core.domain.model.order.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GetAllNotCompletedOrdersQueryHandlerImpl implements GetAllNotCompletedOrdersQueryHandler {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public Result<GetAllNotCompletedOrdersResponse, Error> handle() {
        String jpql = """
                SELECT NEW microarch.delivery.core.application.queries.dto.OrderDto(
                    o.id, o.location
                )
                FROM Order o
                WHERE o.status IN (:statuses)
                """;

        List<OrderDto> orderDtos = em.createQuery(jpql, OrderDto.class)
                .setParameter("statuses", List.of(OrderStatus.CREATED, OrderStatus.ASSIGNED))
                .getResultList();

        GetAllNotCompletedOrdersResponse response = new GetAllNotCompletedOrdersResponse(orderDtos);

        return Result.success(response);
    }
}
