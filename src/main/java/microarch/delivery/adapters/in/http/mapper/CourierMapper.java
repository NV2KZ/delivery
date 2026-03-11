package microarch.delivery.adapters.in.http.mapper;

import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.adapters.in.http.model.Location;
import microarch.delivery.core.application.queries.dto.CourierDto;

public class CourierMapper {
    public static Courier mapCourierDtoToView(CourierDto courierDto) {
        return new Courier(
                courierDto.id(),
                courierDto.name(),
                new Location(courierDto.location().getX(), courierDto.location().getY())
        );
    }
}
