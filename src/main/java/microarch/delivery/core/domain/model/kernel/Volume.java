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

@Embeddable
@Getter
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class Volume extends ValueObject<Volume> {
    private static final int  MIN_VALUE = 1;

    @Column(name = "volume")
    private final int value;

    public static Result<Volume, Error> create(int value) {
        if (value < MIN_VALUE)
            return Result.failure(GeneralErrors.valueMustBeGreaterOrEqual("value", value, MIN_VALUE));

        return Result.success(new Volume(value));
    }

    public static Volume mustCreate(int value) {
        return create(value).getValueOrThrow();
    }

    @Override
    protected Iterable<Object> equalityComponents() {
        return List.of(this.value);
    }

    public boolean isLessThan(Volume other) {
        return this.compareTo(other) < 0;
    }

    public boolean isGreaterThan(Volume other) {
        return this.compareTo(other) > 0;
    }

    public boolean isLessOrEqual(Volume other) {
        return this.compareTo(other) <= 0;
    }

    public boolean isGreaterOrEqual(Volume other) {
        return this.compareTo(other) >= 0;
    }
}