package microarch.delivery.core.domain.model.courier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpeedTest {

    @Test
    void shouldCreateSpeedWithValidValue() {
        // Act
        var result = Speed.create(5);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var speed = result.getValue();
        assertThat(speed.getValue()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100, Integer.MAX_VALUE})
    void shouldCreateSpeedWithAnyPositiveValue(int validValue) {
        // Act
        var result = Speed.create(validValue);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isEqualTo(validValue);
    }

    @Test
    void shouldReturnErrorWhenValueIsLessThanMin() {
        // Act
        var result = Speed.create(0);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }

    @Test
    void shouldBeEqualWhenValuesAreSame() {
        // Arrange
        var speed1 = Speed.mustCreate(5);
        var speed2 = Speed.mustCreate(5);

        // Assert
        assertThat(speed1).isEqualTo(speed2);
        assertThat(speed1.hashCode()).isEqualTo(speed2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenValuesAreDifferent() {
        // Arrange
        var speed1 = Speed.mustCreate(5);
        var speed2 = Speed.mustCreate(7);

        // Assert
        assertThat(speed1).isNotEqualTo(speed2);
        assertThat(speed1.hashCode()).isNotEqualTo(speed2.hashCode());
    }

    @ParameterizedTest
    @CsvSource({
            "3, 5, true",   // 3 меньше 5
            "5, 3, false",  // 5 не меньше 3
            "5, 5, false",  // равные значения
            "1, 10, true",  // минимальное меньше максимального
            "10, 1, false"  // максимальное не меньше минимального
    })
    void shouldCorrectlyCheckIsLessThan(int value1, int value2, boolean expected) {
        // Arrange
        var speed1 = Speed.mustCreate(value1);
        var speed2 = Speed.mustCreate(value2);

        // Act
        boolean result = speed1.isLessThan(speed2);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "5, 3, true",   // 5 больше 3
            "3, 5, false",  // 3 не больше 5
            "5, 5, false",  // равные значения
            "10, 1, true",  // максимальное больше минимального
            "1, 10, false"  // минимальное не больше максимального
    })
    void shouldCorrectlyCheckIsGreaterThan(int value1, int value2, boolean expected) {
        // Arrange
        var speed1 = Speed.mustCreate(value1);
        var speed2 = Speed.mustCreate(value2);

        // Act
        boolean result = speed1.isGreaterThan(speed2);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 5, true",   // 3 <= 5
            "5, 5, true",   // 5 <= 5
            "7, 5, false",  // 7 не <= 5
            "1, 10, true",  // минимальное <= максимального
            "10, 1, false"  // максимальное не <= минимального
    })
    void shouldCorrectlyCheckIsLessOrEqual(int value1, int value2, boolean expected) {
        // Arrange
        var speed1 = Speed.mustCreate(value1);
        var speed2 = Speed.mustCreate(value2);

        // Act
        boolean result = speed1.isLessOrEqual(speed2);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "5, 3, true",   // 5 >= 3
            "5, 5, true",   // 5 >= 5
            "3, 5, false",  // 3 не >= 5
            "10, 1, true",  // максимальное >= минимального
            "1, 10, false"  // минимальное не >= максимального
    })
    void shouldCorrectlyCheckIsGreaterOrEqual(int value1, int value2, boolean expected) {
        // Arrange
        var speed1 = Speed.mustCreate(value1);
        var speed2 = Speed.mustCreate(value2);

        // Act
        boolean result = speed1.isGreaterOrEqual(speed2);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldHandleMinValueCorrectly() {
        // Arrange
        var minSpeed = Speed.mustCreate(1);
        var normalSpeed = Speed.mustCreate(5);
        var alsoMinSpeed = Speed.mustCreate(1);

        // Assert
        assertThat(minSpeed.getValue()).isEqualTo(1);
        assertThat(minSpeed.isLessThan(normalSpeed)).isTrue();
        assertThat(minSpeed.isGreaterThan(normalSpeed)).isFalse();
        assertThat(minSpeed.isLessOrEqual(normalSpeed)).isTrue();
        assertThat(minSpeed.isGreaterOrEqual(normalSpeed)).isFalse();
        assertThat(minSpeed.isLessOrEqual(alsoMinSpeed)).isTrue();
        assertThat(minSpeed.isGreaterOrEqual(alsoMinSpeed)).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenComparingWithNull() {
        // Arrange
        var volume = Speed.mustCreate(5);

        // Act & Assert
        assertThatThrownBy(() -> volume.isLessThan(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> volume.isGreaterThan(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> volume.isLessOrEqual(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> volume.isGreaterOrEqual(null))
                .isInstanceOf(NullPointerException.class);
    }
}