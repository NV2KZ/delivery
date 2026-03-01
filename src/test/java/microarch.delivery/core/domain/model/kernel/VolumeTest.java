package microarch.delivery.core.domain.model.kernel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolumeTest {

    // Тесты создания
    @Test
    void shouldCreateVolumeWithValidValue() {
        // Act
        var result = Volume.create(5);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var volume = result.getValue();
        assertThat(volume.getValue()).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100, Integer.MAX_VALUE})
    void shouldCreateVolumeWithAnyPositiveValue(int validValue) {
        // Act
        var result = Volume.create(validValue);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getValue()).isEqualTo(validValue);
    }

    @Test
    void shouldReturnErrorWhenValueIsLessThanMin() {
        // Act
        var result = Volume.create(0);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }

    // Тесты равенства
    @Test
    void shouldBeEqualWhenValuesAreSame() {
        // Arrange
        var volume1 = Volume.mustCreate(5);
        var volume2 = Volume.mustCreate(5);

        // Assert
        assertThat(volume1).isEqualTo(volume2);
        assertThat(volume1.hashCode()).isEqualTo(volume2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenValuesAreDifferent() {
        // Arrange
        var volume1 = Volume.mustCreate(5);
        var volume2 = Volume.mustCreate(7);

        // Assert
        assertThat(volume1).isNotEqualTo(volume2);
        assertThat(volume1.hashCode()).isNotEqualTo(volume2.hashCode());
    }

    // Тесты сравнения isLessThan
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
        var volume1 = Volume.mustCreate(value1);
        var volume2 = Volume.mustCreate(value2);

        // Act
        boolean result = volume1.isLessThan(volume2);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    // Тесты сравнения isGreaterThan
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
        var volume1 = Volume.mustCreate(value1);
        var volume2 = Volume.mustCreate(value2);

        // Act
        boolean result = volume1.isGreaterThan(volume2);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    // Тесты сравнения isLessOrEqual
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
        var volume1 = Volume.mustCreate(value1);
        var volume2 = Volume.mustCreate(value2);

        // Act
        boolean result = volume1.isLessOrEqual(volume2);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    // Тесты сравнения isGreaterOrEqual
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
        var volume1 = Volume.mustCreate(value1);
        var volume2 = Volume.mustCreate(value2);

        // Act
        boolean result = volume1.isGreaterOrEqual(volume2);

        // Assert
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowExceptionWhenComparingWithNull() {
        // Arrange
        var volume = Volume.mustCreate(5);

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

    @Test
    void shouldHandleMinValueCorrectly() {
        // Arrange
        var minVolume = Volume.mustCreate(1);
        var normalVolume = Volume.mustCreate(5);
        var alsoMinVolume = Volume.mustCreate(1);

        // Assert
        assertThat(minVolume.getValue()).isEqualTo(1);

        // Сравнения с большим значением
        assertThat(minVolume.isLessThan(normalVolume)).isTrue();
        assertThat(minVolume.isGreaterThan(normalVolume)).isFalse();
        assertThat(minVolume.isLessOrEqual(normalVolume)).isTrue();
        assertThat(minVolume.isGreaterOrEqual(normalVolume)).isFalse();

        // Сравнения с таким же минимальным
        assertThat(minVolume.isLessThan(alsoMinVolume)).isFalse();
        assertThat(minVolume.isGreaterThan(alsoMinVolume)).isFalse();
        assertThat(minVolume.isLessOrEqual(alsoMinVolume)).isTrue();
        assertThat(minVolume.isGreaterOrEqual(alsoMinVolume)).isTrue();
    }
}
