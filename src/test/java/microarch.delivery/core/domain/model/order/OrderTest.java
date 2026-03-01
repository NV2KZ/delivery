package microarch.delivery.core.domain.model.order;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    @Test
    void shouldCreateOrderWithValidParameters() {
        // Arrange
        UUID basketId = UUID.randomUUID();
        Location location = Location.mustCreate(5, 5);
        var volume = Volume.mustCreate(10);

        // Act
        var result = Order.create(basketId, location, volume);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var order = result.getValue();
        assertThat(order.getId()).isEqualTo(basketId);
        assertThat(order.getLocation()).isEqualTo(location);
        assertThat(order.getVolume()).isEqualTo(volume);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCourierId()).isNull();
    }

    @Test
    void shouldThrowExceptionWhenBasketIdIsNull() {
        // Arrange
        Location location = Location.mustCreate(5, 5);

        // Assert
        assertThatThrownBy(() -> Order.create(null, location, Volume.mustCreate(10)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("basketId");
    }

    @Test
    void shouldThrowExceptionWhenLocationIsNull() {
        // Arrange
        UUID basketId = UUID.randomUUID();

        // Assert
        assertThatThrownBy(() -> Order.create(basketId, null, Volume.mustCreate(10)))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("location");
    }

    @Test
    void shouldAssignCourierToOrder() {
        // Arrange
        UUID basketId = UUID.randomUUID();
        Location location = Location.mustCreate(5, 5);
        var order = Order.mustCreate(basketId, location, Volume.mustCreate(10));
        UUID courierId = UUID.randomUUID();

        // Act
        var result = order.assign(courierId);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(order.getCourierId()).isEqualTo(courierId);
    }

    @Test
    void shouldThrowExceptionWhenAssigningWithNullCourierId() {
        // Arrange
        UUID basketId = UUID.randomUUID();
        Location location = Location.mustCreate(5, 5);
        var order = Order.mustCreate(basketId, location, Volume.mustCreate(10));

        // Act & Assert
        assertThatThrownBy(() -> order.assign(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("courierId");
    }

    @Test
    void shouldAllowReassigningToDifferentCourier() {
        // Arrange
        UUID basketId = UUID.randomUUID();
        Location location = Location.mustCreate(5, 5);
        var order = Order.mustCreate(basketId, location, Volume.mustCreate(10));
        UUID firstCourierId = UUID.randomUUID();
        UUID secondCourierId = UUID.randomUUID();

        // Act
        order.assign(firstCourierId);
        var result = order.assign(secondCourierId);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ASSIGNED);
        assertThat(order.getCourierId()).isEqualTo(secondCourierId);
    }

    @Test
    void shouldCompleteAssignedOrder() {
        // Arrange
        UUID basketId = UUID.randomUUID();
        Location location = Location.mustCreate(5, 5);
        var order = Order.mustCreate(basketId, location, Volume.mustCreate(10));
        UUID courierId = UUID.randomUUID();
        order.assign(courierId);

        // Act
        var result = order.complete();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getCourierId()).isEqualTo(courierId);
    }

    @Test
    void shouldNotCompleteOrderWhenNotAssigned() {
        // Arrange
        UUID basketId = UUID.randomUUID();
        Location location = Location.mustCreate(5, 5);
        var order = Order.mustCreate(basketId, location, Volume.mustCreate(10));

        // Act
        var result = order.complete();

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("order.is.not.assigned");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getCourierId()).isNull();
    }

    @Test
    void shouldNotCompleteAlreadyCompletedOrder() {
        // Arrange
        UUID basketId = UUID.randomUUID();
        Location location = Location.mustCreate(5, 5);
        var order = Order.mustCreate(basketId, location, Volume.mustCreate(10));
        UUID courierId = UUID.randomUUID();
        order.assign(courierId);
        order.complete();

        // Act
        var result = order.complete();

        // Assert
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError().getCode()).isEqualTo("order.is.not.assigned");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }
}
