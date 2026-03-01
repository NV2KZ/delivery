package microarch.delivery.core.domain.model.kernel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import libs.ddd.ValueObject;
import libs.errs.Error;
import libs.errs.GeneralErrors;
import libs.errs.Result;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Location extends ValueObject<Location> {

    @Column(name = "x_coordinate")
    private final int x;

    @Column(name = "y_coordinate")
    private final int y;

    public static Result<Location, Error> create(int x, int y) {
        if (x < 1 || x > 10) return Result.failure(GeneralErrors.valueIsOutOfRange("x", x, 1, 10));
        if (y < 1 || y > 10) return Result.failure(GeneralErrors.valueIsOutOfRange("y", y, 1, 10));

        return Result.success(new Location(x, y));
    }

    public static Location mustCreate(int x, int y) {
        return create(x, y).getValueOrThrow();
    }

    public Result<Integer, Error> distanceTo(Location target) {
        Objects.requireNonNull(target, "target");

        int distance = Math.abs(this.x - target.x) + Math.abs(this.y - target.y);

        return Result.success(distance);
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(this.x, this.y);
    }
}