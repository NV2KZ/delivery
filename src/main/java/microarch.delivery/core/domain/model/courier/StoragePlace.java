package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.Column;
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

import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "storage_place")
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
public class StoragePlace extends BaseEntity<UUID> {

    @Column(name = "name")
    private final String name;

    @Column(name = "total_volume")
    private final int totalVolume;

    @Column(name = "order_id")
    private UUID orderId;

    private StoragePlace(String name, int totalVolume) {
        super(UUID.randomUUID());
        this.name = name;
        this.totalVolume = totalVolume;
        this.orderId = null;
    }

    public static Result<StoragePlace, Error> create(String name, int totalVolume) {
        if (name == null || name.isBlank()) return Result.failure(GeneralErrors.valueIsRequired("name"));
        if (totalVolume < 1)
            return Result.failure(GeneralErrors.valueMustBeGreaterOrEqual("totalVolume", totalVolume, 1));

        var storagePlace = new StoragePlace(name, totalVolume);
        return Result.success(storagePlace);
    }

    public static StoragePlace mustCreate(String name, int totalVolume) {
        return create(name, totalVolume).getValueOrThrow();
    }

    public boolean canPlaceOrder(int orderVolume) {
        return isEmpty() && orderVolume <= this.totalVolume;
    }

    public UnitResult<Error> placeOrder(UUID orderId, int orderVolume) {
        Objects.requireNonNull(orderId, "orderId");

        if (!isEmpty()) {
            return UnitResult.failure(Error.of("storage_place.not_empty",
                    "Cannot place order in storage place that already contains an order"));
        }

        if (orderVolume > this.totalVolume) {
            return UnitResult.failure(Error.of("storage_place.insufficient_capacity",
                    String.format("Order volume %d exceeds storage place capacity %d", orderVolume, this.totalVolume)));
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