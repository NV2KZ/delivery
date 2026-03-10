package microarch.delivery.core.application.queries;

import microarch.delivery.core.application.queries.dto.OrderDto;

import java.util.List;

public record GetAllNotCompletedOrdersResponse(List<OrderDto> orders) {
}
