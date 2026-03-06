package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CourierJpaRepository extends JpaRepository<Courier, UUID> {

    @Query(
    """
        SELECT c
        FROM Courier c
        WHERE NOT EXISTS (
            SELECT 1
            FROM StoragePlace sp
            WHERE sp MEMBER OF c.storagePlaces
            AND sp.orderId IS NOT NULL
        )
    """
    )
    List<Courier> findAllAvailable();
}
