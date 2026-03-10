package microarch.delivery.core.application.queries;

import microarch.delivery.core.application.queries.dto.CourierDto;

import java.util.List;

public record GetAllCouriersResponse(List<CourierDto> couriers) {
}
