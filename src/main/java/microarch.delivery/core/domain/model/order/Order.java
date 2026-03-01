package microarch.delivery.core.domain.model.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "order")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends Aggregate<UUID> {

    @Embedded
    private Location location;

    @Embedded
    private Volume volume;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "courier_id")
    private UUID courierId;

    private Order(UUID id, Location location, Volume volume) {
        super(id);
        this.location = location;
        this.volume = volume;
        this.status = OrderStatus.CREATED;
    }

    public static Result<Order, Error> create(UUID basketId, Location location, Volume volume) {
        Objects.requireNonNull(basketId, "basketId");
        Objects.requireNonNull(location, "location");
        Objects.requireNonNull(location, "volume");

        var order = new Order(basketId, location, volume);
        return Result.success(order);
    }

    public static Order mustCreate(UUID basketId, Location location, Volume volume) {
        return create(basketId, location, volume).getValueOrThrow();
    }

    public UnitResult<Error> assign(UUID courierId) {
        Objects.requireNonNull(courierId, "courierId");
        this.courierId = courierId;
        this.status = OrderStatus.ASSIGNED;
        return UnitResult.success();
    }

    public UnitResult<Error> complete() {
        if (this.status != OrderStatus.ASSIGNED) {
            return UnitResult.failure(Errors.orderIsNotAssigned());
        } else {
            this.status = OrderStatus.COMPLETED;
            return UnitResult.success();
        }
    }

    public static class Errors {
        public static Error orderIsNotAssigned() {
            return Error.of("order.is.not.assigned",
                    "Заказ ни на кого не назначен либо уже завершён");
        }
    }
}
