package microarch.delivery.core.domain.model.courier;

import microarch.delivery.core.domain.model.kernel.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CourierTest {

    @Test
    void shouldCreateCourierWithValidParameters() {
        // Arrange
        String name = "Иван";
        Speed speed = Speed.mustCreate(2);
        Location location = Location.mustCreate(5, 5);

        // Act
        var result = Courier.create(name, speed, location);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var courier = result.getValue();
        assertThat(courier.getName()).isEqualTo(name);
        assertThat(courier.getSpeed()).isEqualTo(speed);
        assertThat(courier.getLocation()).isEqualTo(location);
        assertThat(courier.getId()).isNotNull();

        // Проверяем, что создалась сумка по умолчанию
        assertThat(courier.getStoragePlaces()).hasSize(1);
        var defaultBag = courier.getStoragePlaces().get(0);
        assertThat(defaultBag.getName()).isEqualTo("Сумка");
        assertThat(defaultBag.getTotalVolume()).isEqualTo(10);
        assertThat(defaultBag.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldReturnErrorWhenNameIsInvalid(String invalidName) {
        // Arrange
        Location location = Location.mustCreate(5, 5);

        // Act
        var result = Courier.create(invalidName, Speed.mustCreate(2), location);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }

    @Test
    void shouldReturnErrorWhenLocationIsNull() {
        // Act
        var result = Courier.create("Иван", Speed.mustCreate(2), null);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }

    @Test
    void shouldReturnErrorWhenSpeedIsNull() {
        // Act
        var result = Courier.create("Иван", null, Location.mustCreate(5, 5));

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }

    @Test
    void shouldAddStoragePlaceSuccessfully() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));

        // Act
        var result = courier.addStoragePlace("Рюкзак", 20);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getStoragePlaces()).hasSize(2);

        var newPlace = courier.getStoragePlaces().get(1);
        assertThat(newPlace.getName()).isEqualTo("Рюкзак");
        assertThat(newPlace.getTotalVolume()).isEqualTo(20);
        assertThat(newPlace.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnErrorWhenAddingInvalidStoragePlace() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));

        // Act
        var result = courier.addStoragePlace("", 20);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
        assertThat(courier.getStoragePlaces()).hasSize(1); // размер не изменился
    }

    @Test
    void shouldReturnTrueWhenThereIsSuitableStoragePlace() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));
        courier.addStoragePlace("Рюкзак", 20);

        // Assert
        assertThat(courier.canPlaceOrder(5)).isTrue();  // в сумку
        assertThat(courier.canPlaceOrder(10)).isTrue(); // ровно под сумку
        assertThat(courier.canPlaceOrder(15)).isTrue(); // в рюкзак
    }

    @Test
    void shouldReturnFalseWhenNoSuitableStoragePlace() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));

        // Assert
        assertThat(courier.canPlaceOrder(11)).isFalse();
        assertThat(courier.canPlaceOrder(20)).isFalse();
    }

    @Test
    void shouldTakeOrderAndPlaceInExactlyMatchingVolume() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));
        UUID orderId = UUID.randomUUID();

        // Act
        var result = courier.takeOrder(orderId, 10); // ровно под сумку

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var bag = courier.getStoragePlaces().get(0);
        assertThat(bag.isEmpty()).isFalse();
        assertThat(bag.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void shouldNotTakeOrderWhenNoSuitablePlace() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));
        UUID orderId = UUID.randomUUID();

        // Act
        var result = courier.takeOrder(orderId, 11); // больше чем 10

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("courier.cannot.take.order");
        assertThat(courier.getStoragePlaces().get(0).isEmpty()).isTrue(); // место свободно
    }

    @Test
    void shouldThrowExceptionWhenTakingOrderWithNullOrderId() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));

        // Assert
        assertThatThrownBy(() -> courier.takeOrder(null, 5))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    void shouldCompleteOrderAndFreeStoragePlace() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));
        UUID orderId = UUID.randomUUID();
        courier.takeOrder(orderId, 5);

        // Act
        var result = courier.completeOrder(orderId);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var bag = courier.getStoragePlaces().get(0);
        assertThat(bag.isEmpty()).isTrue();
        assertThat(bag.getOrderId()).isNull();
    }

    @Test
    void shouldNotCompleteOrderWhenOrderNotFound() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));
        UUID orderId = UUID.randomUUID();
        UUID wrongOrderId = UUID.randomUUID();
        courier.takeOrder(orderId, 5);

        // Act
        var result = courier.completeOrder(wrongOrderId);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("courier.cannot.complete.order");
        assertThat(courier.getStoragePlaces().get(0).isEmpty()).isFalse(); // заказ все еще там
    }

    @Test
    void shouldThrowExceptionWhenCompletingWithNullOrderId() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));

        // Act & Assert
        assertThatThrownBy(() -> courier.completeOrder(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    void shouldCalculateDeliveryTimeCorrectly() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(1, 1));
        var target = Location.mustCreate(5, 5);

        // Act
        var result = courier.calculateDeliveryTime(target);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getValue()).isEqualTo(4); // 8/2 = 4 такта
    }

    @Test
    void shouldReturnErrorWhenTargetIsNull() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(1, 1));

        // Assert
        assertThatThrownBy(() -> courier.calculateDeliveryTime(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("target");
    }

    @Test
    void shouldMoveTowardsTargetWithinSpeed() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(1, 1));
        var target = Location.mustCreate(4, 5);

        // Act
        var result = courier.move(target);

        // Assert
        assertThat(result.isSuccess()).isTrue();

        assertThat(courier.getLocation().getX()).isEqualTo(3);
        assertThat(courier.getLocation().getY()).isEqualTo(1);
    }

    @Test
    void shouldMoveExactlyToTargetWhenWithinRange() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(5), Location.mustCreate(1, 1));
        var target = Location.mustCreate(3, 4);

        // Act
        var result = courier.move(target);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(target);
    }

    @Test
    void shouldNotMoveWhenTargetIsCurrentLocation() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(5, 5));
        var target = Location.mustCreate(5, 5);

        // Act
        var result = courier.move(target);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(courier.getLocation()).isEqualTo(target);
    }

    @Test
    void shouldThrowExceptionWhenMoveTargetIsNull() {
        // Arrange
        var courier = Courier.mustCreate("Иван", Speed.mustCreate(2), Location.mustCreate(1, 1));

        // Assert
        assertThatThrownBy(() -> courier.move(null))
                .isInstanceOf(NullPointerException.class);
    }
}
