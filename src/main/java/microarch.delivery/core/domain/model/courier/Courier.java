package microarch.delivery.core.domain.model.courier;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import libs.ddd.Aggregate;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import libs.errs.UnitResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import microarch.delivery.core.domain.model.kernel.Location;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "courier")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Courier extends Aggregate<UUID> {

    private static final String DEFAULT_STORAGE_PLACE_NAME = "Сумка";
    private static final int DEFAULT_STORAGE_PLACE_VOLUME = 10;

    @Column(name = "name")
    private String name;

    @Column(name = "speed")
    private int speed;

    @Embedded
    private Location location;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @JoinColumn(name = "courier_id", nullable = false)
    private final List<StoragePlace> storagePlaces = new ArrayList<>();

    private Courier(String name, int speed, Location location) {
        super(UUID.randomUUID());
        this.name = name;
        this.speed = speed;
        this.location = location;
        this.storagePlaces.add(
                StoragePlace.mustCreate(
                        DEFAULT_STORAGE_PLACE_NAME,
                        DEFAULT_STORAGE_PLACE_VOLUME
                )
        );
    }

    public static Result<Courier, Error> create(String name, int speed, Location location) {
        if (name == null || name.isBlank()) return Result.failure(GeneralErrors.valueIsRequired("name"));
        if (location == null) return Result.failure(GeneralErrors.valueIsRequired("location"));
        if (speed < 1)
            return Result.failure(GeneralErrors.valueMustBeGreaterOrEqual("speed", speed, 1));

        var order = new Courier(name, speed, location);
        return Result.success(order);
    }

    public static Courier mustCreate(String name, int speed, Location location) {
        return create(name, speed, location).getValueOrThrow();
    }

    public UnitResult<Error> addStoragePlace(String name, int volume) {
        var storagePlaceResult = StoragePlace.create(name, volume);
        if (storagePlaceResult.isFailure()) {
            return UnitResult.failure(storagePlaceResult.getError());
        }
        storagePlaces.add(storagePlaceResult.getValue());
        return UnitResult.success();
    }

    public boolean canPlaceOrder(int orderVolume) {
        return storagePlaces.stream()
                .anyMatch(storagePlace -> storagePlace.canPlaceOrder(orderVolume));
    }

    public UnitResult<Error> takeOrder(UUID orderId, int orderVolume) {
        Objects.requireNonNull(orderId, "orderId");

        return storagePlaces.stream()
                .filter(storagePlace -> storagePlace.canPlaceOrder(orderVolume))
                .min(Comparator.comparingInt(StoragePlace::getTotalVolume))
                .map(storagePlace -> storagePlace.placeOrder(orderId, orderVolume))
                .orElse(UnitResult.failure(Errors.canNotTakeOrder(orderVolume)));
    }

    public UnitResult<Error> completeOrder(UUID orderId) {
        Objects.requireNonNull(orderId, "orderId");

        var storagePlaceWithOrder = storagePlaces.stream()
                .filter(storagePlace -> orderId.equals(storagePlace.getOrderId()))
                .findFirst();

        if (storagePlaceWithOrder.isEmpty()) {
            return UnitResult.failure(Errors.orderIsNotFoundInStoragePlaces(orderId));
        }

        return storagePlaceWithOrder.get().removeOrder();
    }

    public Result<Integer, Error> calculateDeliveryTime(Location target) {
        Objects.requireNonNull(target, "target");

        var distanceResult = this.location.distanceTo(target);
        if (distanceResult.isFailure()) {
            return Result.failure(distanceResult.getError());
        }

        int distance = distanceResult.getValue();
        int ticks = (int) Math.ceil((double) distance / this.speed);

        return Result.success(ticks);
    }

    public UnitResult<Error> move(Location target) {
        Objects.requireNonNull(target);

        int difX = target.getX() - location.getX();
        int difY = target.getY() - location.getY();
        int cruisingRange = speed;

        int moveX = Math.max(-cruisingRange, Math.min(difX, cruisingRange));
        cruisingRange -= Math.abs(moveX);

        int moveY = Math.max(-cruisingRange, Math.min(difY, cruisingRange));

        Result<Location, Error> locationCreateResult = Location.create(
                location.getX() + moveX,
                location.getY() + moveY
        );

        if (locationCreateResult.isFailure()) {
            return UnitResult.failure(locationCreateResult.getError());
        }

        this.location = locationCreateResult.getValue();
        return UnitResult.success();
    }

    public static class Errors {
        public static Error orderIsNotFoundInStoragePlaces(UUID orderId) {
            return Error.of("courier.cannot.complete.order",
                    String.format("Заказ %s не найден ни в одном месте хранения", orderId));
        }

        public static Error canNotTakeOrder(int orderVolume) {
            return Error.of("courier.cannot.take.order",
                    String.format("Невозможно принять заказ объемом %d - нет подходящего места для хранения.", orderVolume));
        }
    }
}