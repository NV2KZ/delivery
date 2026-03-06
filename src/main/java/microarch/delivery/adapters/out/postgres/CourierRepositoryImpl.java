package microarch.delivery.adapters.out.postgres;

import lombok.AllArgsConstructor;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.ports.CourierRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class CourierRepositoryImpl implements CourierRepository {

    private final CourierJpaRepository jpa;

    @Override
    public Courier save(Courier courier) {
        return jpa.save(courier);
    }

    @Override
    public Optional<Courier> findById(UUID courierId) {
        return jpa.findById(courierId);
    }

    @Override
    public List<Courier> findAllAvailable() {
        return jpa.findAllAvailable();
    }
}
