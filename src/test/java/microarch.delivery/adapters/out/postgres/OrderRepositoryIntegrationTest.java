package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.domain.model.order.OrderStatus;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrderRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    OrderRepository repository;

    @Autowired
    OrderJpaRepository jpaRepository;

    @BeforeEach
    void tearDown() {
        // Очищаем таблицу через JPA репозиторий перед каждым тестом
        jpaRepository.deleteAll();
    }

    @Test
    void canSaveAndFindById() {
        // Arrange
        var orderId = UUID.randomUUID();
        // Создаем заказ с обязательными параметрами: ID корзины, локация, объем
        var order = Order.mustCreate(orderId, Location.mustCreate(1, 10), Volume.mustCreate(5));

        // Act
        var saved = repository.save(order);
        var loaded = repository.findById(saved.getId());

        // Assert
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getId()).isEqualTo(orderId);
        assertThat(loaded.get().getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(loaded.get().getLocation()).isEqualTo(order.getLocation());
        assertThat(loaded.get().getVolume()).isEqualTo(order.getVolume());
    }

    @Test
    void returnsEmptyWhenOrderNotFound() {
        // Arrange
        var nonExistentId = UUID.randomUUID();

        // Act
        var result = repository.findById(nonExistentId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void canFindAnyCreatedOrder() {
        // Arrange
        var order1 = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(1, 1), Volume.mustCreate(1));
        var order2 = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(2, 2), Volume.mustCreate(2));

        repository.save(order1);
        repository.save(order2);

        // Act
        // Метод findAnyCreated() должен вернуть Optional с любым заказом со статусом CREATED
        var result = repository.findAnyCreated();

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(result.get().getId()).isIn(order1.getId(), order2.getId());
    }

    @Test
    void canFindAllAssignedOrders() {
        // Arrange
        var order1 = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(1, 1), Volume.mustCreate(1));
        var order2 = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(2, 2), Volume.mustCreate(2));

        var courierId = UUID.randomUUID();

        // Назначаем курьеров, что меняет статус на ASSIGNED
        order1.assign(courierId);
        order2.assign(courierId);

        repository.save(order1);
        repository.save(order2);

        // Act
        List<Order> assignedOrders = repository.findAllAssigned();

        // Assert
        assertThat(assignedOrders).hasSize(2);
        assertThat(assignedOrders).extracting(Order::getStatus)
                .containsOnly(OrderStatus.ASSIGNED);
        assertThat(assignedOrders).extracting(Order::getCourierId)
                .containsOnly(courierId);
        assertThat(assignedOrders).extracting(Order::getId)
                .containsExactlyInAnyOrder(order1.getId(), order2.getId());
    }

    @Test
    void findAllAssignedReturnsEmptyWhenNoAssignedOrders() {
        // Arrange
        var order = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(1, 1), Volume.mustCreate(1));
        // Статус по умолчанию CREATED, поэтому в выборку ASSIGNED он не попадет
        repository.save(order);

        // Act
        List<Order> assignedOrders = repository.findAllAssigned();

        // Assert
        assertThat(assignedOrders).isEmpty();
    }

    @Test
    void shouldNotFindAssignedOrderIfNotSaved() {
        // Arrange
        var order = Order.mustCreate(UUID.randomUUID(), Location.mustCreate(1, 1), Volume.mustCreate(1));
        var courierId = UUID.randomUUID();

        // Меняем статус в памяти, но НЕ сохраняем в БД
        order.assign(courierId);

        // Act
        List<Order> assignedOrders = repository.findAllAssigned();

        // Assert
        assertThat(assignedOrders).isEmpty();
    }
}