package microarch.delivery.core.domain.model.courier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoragePlaceTest {

    // Тесты создания
    @Test
    void shouldCreateStoragePlaceWithValidParameters() {
        // Act
        var result = StoragePlace.create("Main Backpack", 100);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var storagePlace = result.getValue();
        assertThat(storagePlace.getName()).isEqualTo("Main Backpack");
        assertThat(storagePlace.getTotalVolume()).isEqualTo(100);
        assertThat(storagePlace.isEmpty()).isTrue();
        assertThat(storagePlace.getOrderId()).isNull();
        assertThat(storagePlace.getId()).isNotNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void shouldReturnErrorWhenNameIsInvalid(String invalidName) {
        // Act
        var result = StoragePlace.create(invalidName, 100);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldReturnErrorWhenTotalVolumeIsLessThanOne(int invalidVolume) {
        // Act
        var result = StoragePlace.create("Backpack", invalidVolume);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isNotNull();
    }

    // Тесты canPlaceOrder
    @Test
    void shouldAllowPlacingOrderWhenStorageIsEmptyAndHasEnoughCapacity() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);
        int orderVolume = 80;

        // Act
        boolean canPlace = storagePlace.canPlaceOrder(orderVolume);

        // Assert
        assertThat(canPlace).isTrue();
    }

    @Test
    void shouldNotAllowPlacingOrderWhenStorageIsNotEmpty() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);
        var orderId = UUID.randomUUID();
        storagePlace.placeOrder(orderId, 50);

        // Act
        boolean canPlace = storagePlace.canPlaceOrder(30);

        // Assert
        assertThat(canPlace).isFalse();
    }

    @Test
    void shouldNotAllowPlacingOrderWhenOrderVolumeExceedsCapacity() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);

        // Act
        boolean canPlace = storagePlace.canPlaceOrder(150);

        // Assert
        assertThat(canPlace).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "100, 50, true",   // достаточно места
            "100, 100, true",  // ровно по объему
            "100, 101, false", // превышение объема
            "50, 30, true",    // меньше объема
            "50, 51, false"    // чуть больше объема
    })
    void shouldCheckCanPlaceOrderCorrectly(int totalVolume, int orderVolume, boolean expected) {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", totalVolume);

        // Act
        boolean canPlace = storagePlace.canPlaceOrder(orderVolume);

        // Assert
        assertThat(canPlace).isEqualTo(expected);
    }

    @Test
    void shouldPlaceOrderSuccessfully() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);
        var orderId = UUID.randomUUID();

        // Act
        var result = storagePlace.placeOrder(orderId, 80);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(storagePlace.isEmpty()).isFalse();
        assertThat(storagePlace.getOrderId()).isEqualTo(orderId);
    }

    @Test
    void shouldNotPlaceOrderWhenStorageIsNotEmpty() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);
        var firstOrderId = UUID.randomUUID();
        storagePlace.placeOrder(firstOrderId, 50);
        var secondOrderId = UUID.randomUUID();

        // Act
        var result = storagePlace.placeOrder(secondOrderId, 30);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("storage_place.not_empty");
        assertThat(storagePlace.getOrderId()).isEqualTo(firstOrderId);
    }

    @Test
    void shouldNotPlaceOrderWhenVolumeExceedsCapacity() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);
        var orderId = UUID.randomUUID();

        // Act
        var result = storagePlace.placeOrder(orderId, 150);

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("storage_place.insufficient_capacity");
        assertThat(storagePlace.isEmpty()).isTrue();
        assertThat(storagePlace.getOrderId()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenOrderIdIsNull() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);

        // Assert
        assertThatThrownBy(() -> storagePlace.placeOrder(null, 50))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("orderId");
    }

    @Test
    void shouldRemoveOrderSuccessfully() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);
        var orderId = UUID.randomUUID();
        storagePlace.placeOrder(orderId, 80);

        // Act
        var result = storagePlace.removeOrder();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(storagePlace.isEmpty()).isTrue();
        assertThat(storagePlace.getOrderId()).isNull();
    }

    @Test
    void shouldNotRemoveOrderWhenStorageIsEmpty() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);

        // Act
        var result = storagePlace.removeOrder();

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("storage_place.already_empty");
        assertThat(storagePlace.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnTrueWhenStorageIsEmpty() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);

        // Assert
        assertThat(storagePlace.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenStorageHasOrder() {
        // Arrange
        var storagePlace = StoragePlace.mustCreate("Backpack", 100);
        storagePlace.placeOrder(UUID.randomUUID(), 50);

        // Assert
        assertThat(storagePlace.isEmpty()).isFalse();
    }
}