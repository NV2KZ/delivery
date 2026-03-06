package microarch.delivery.core.ports;

import microarch.delivery.core.domain.model.courier.Courier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourierRepository {
    Courier save(Courier courier);

    Optional<Courier> findById(UUID courierId);

    List<Courier> findAllAvailable();
}
