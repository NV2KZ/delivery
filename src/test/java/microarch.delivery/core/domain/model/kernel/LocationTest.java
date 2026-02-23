package microarch.delivery.core.domain.model.kernel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class LocationTest {
    @Test
    void derivedAggregate() {
        assertThat(Location.class.getSuperclass().getSimpleName()).isEqualTo("ValueObject");
    }

    @Test
    void shouldBeCorrectWhenParamsAreCorrectOnCreated() {
        // Act
        var result = Location.create(1, 10);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue().getX()).isEqualTo(1);
        assertThat(result.getValue().getY()).isEqualTo(10);
    }


    @ParameterizedTest
    @CsvSource({
            "0, 5",    // X меньше 1
            "11, 5",   // X больше 10
            "5, 0",   // Y меньше 1
            "5, 11"    // Y больше 10
    })
    void shouldReturnErrorWhenPropertiesAreOutOfRange(int x, int y) {
        // Act
        var result = Location.create(x, y);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }

    @Test
    public void shouldBeEqualWhenAllPropertiesIsEqual() {
        // Arrange
        var first = Location.mustCreate(5, 6);
        var second = Location.mustCreate(5, 6);

        // Act
        var result = first.equals(second);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    public void shouldBeNotEqualWhenOneOfPropertiesIsNotEqual() {
        // Arrange
        var first = Location.mustCreate(6, 5);
        var second = Location.mustCreate(6, 6);

        // Act
        var result = first.equals(second);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void shouldCalculateDistanceBetweenTwoLocations() {
        // Arrange
        var location = Location.mustCreate(1, 1);
        var target = Location.mustCreate(4, 5);
        var expectedDistance = 7;

        // Act
        var result = location.distanceTo(target);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(expectedDistance);
    }

    @Test
    void shouldReturnZeroWhenLocationsAreSame() {
        // Arrange
        var location = Location.mustCreate(3, 7);
        var target = Location.mustCreate(3, 7);

        // Act
        var result = location.distanceTo(target);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(0);
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1, 1, 10, 9",
            "1, 1, 10, 1, 9",
            "1, 1, 10, 10, 18"
    })
    void shouldCalculateCorrectDistanceForDifferentDirections(int x1, int y1, int x2, int y2, int expectedDistance) {
        // Arrange
        var location = Location.mustCreate(x1, y1);
        var target = Location.mustCreate(x2, y2);

        // Act
        var result = location.distanceTo(target);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(expectedDistance);
    }

    @Test
    void shouldReturnErrorWhenTargetIsNull() {
        // Arrange
        var location = Location.mustCreate(1, 1);

        // Act
        var result = location.distanceTo(null);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }
}
