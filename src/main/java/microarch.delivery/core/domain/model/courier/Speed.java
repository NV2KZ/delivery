package microarch.delivery.core.domain.model.courier;

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

@Embeddable
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Speed extends ValueObject<Speed> {

    private static final int MIN_VALUE = 1;

    @Column(name = "speed")
    private final int value;

    public static Result<Speed, Error> create(int value) {
        if (value < MIN_VALUE)
            return Result.failure(GeneralErrors.valueMustBeGreaterOrEqual("value", value, MIN_VALUE));

        return Result.success(new Speed(value));
    }

    public static Speed mustCreate(int value) {
        return create(value).getValueOrThrow();
    }

    public boolean isLessThan(Speed other) {
        return this.compareTo(other) < 0;
    }

    public boolean isGreaterThan(Speed other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLessOrEqual(Speed other) {
        return this.compareTo(other) <= 0;
    }

    public boolean isGreaterOrEqual(Speed other) {
        return this.compareTo(other) >= 0;
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(this.value);
    }
}
