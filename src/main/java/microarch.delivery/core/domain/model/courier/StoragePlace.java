package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import libs.ddd.BaseEntity;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Volume;

import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "storage_place")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class StoragePlace extends BaseEntity<UUID> {

    @Column(name = "name")
    private final String name;

    @Embedded
    private final Volume totalVolume;

    @Column(name = "order_id")
    private UUID orderId;

    private StoragePlace(String name, Volume totalVolume) {
        super(UUID.randomUUID());
        this.name = name;
        this.totalVolume = totalVolume;
        this.orderId = null;
    }

    public static Result<StoragePlace, Error> create(String name, Volume totalVolume) {
        if (name == null || name.isBlank()) return Result.failure(GeneralErrors.valueIsRequired("name"));
        if (totalVolume == null) return Result.failure(GeneralErrors.valueIsRequired("totalVolume"));

        var storagePlace = new StoragePlace(name, totalVolume);
        return Result.success(storagePlace);
    }

    public static StoragePlace mustCreate(String name, Volume totalVolume) {
        return create(name, totalVolume).getValueOrThrow();
    }

    public boolean canPlaceOrder(Volume orderVolume) {
        return isEmpty() && this.totalVolume.isGreaterOrEqual(orderVolume);
    }

    public UnitResult<Error> placeOrder(UUID orderId, Volume orderVolume) {
        Objects.requireNonNull(orderId, "orderId");

        if (!isEmpty()) {
            return UnitResult.failure(Error.of("storage_place.not_empty",
                    "Cannot place order in storage place that already contains an order"));
        }

        if (this.totalVolume.isLessThan(orderVolume)) {
            return UnitResult.failure(
                    Error.of("storage_place.insufficient_capacity",
                            String.format(
                                    "Order volume %d exceeds storage place capacity %d",
                                    orderVolume.getValue(),
                                    this.totalVolume.getValue()
                            )
                    )
            );
        }

        this.orderId = orderId;
        return UnitResult.success();
    }

    public UnitResult<Error> removeOrder() {
        if (isEmpty()) {
            return UnitResult.failure(Error.of("storage_place.already_empty",
                    "Cannot remove order from empty storage place"));
        }

        this.orderId = null;
        return UnitResult.success();
    }

    public boolean isEmpty() {
        return orderId == null;
    }
}