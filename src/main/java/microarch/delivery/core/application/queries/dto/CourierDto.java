package microarch.delivery.core.application.queries.dto;

import microarch.delivery.core.domain.model.kernel.Location;

import java.util.UUID;

public record CourierDto(UUID id, String name, Location location) {

}
