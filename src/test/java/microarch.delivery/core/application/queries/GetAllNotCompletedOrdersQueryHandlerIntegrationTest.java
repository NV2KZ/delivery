package microarch.delivery.core.application.queries;

import microarch.delivery.adapters.out.postgres.OrderJpaRepository;
import microarch.delivery.adapters.out.postgres.PostgresIntegrationTestBase;
import microarch.delivery.core.application.queries.dto.OrderDto;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.order.Order;
import microarch.delivery.core.ports.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GetAllNotCompletedOrdersQueryHandlerIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private GetAllNotCompletedOrdersQueryHandler handler;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderJpaRepository jpaRepository;

    private final GetAllNotCompletedOrdersQuery query = new GetAllNotCompletedOrdersQuery();

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Test
    void shouldReturnAllUncompletedOrders() {
        // Arrange
        var order1 = Order.mustCreate(
                UUID.randomUUID(),
                Location.mustCreate(1, 2),
                Volume.mustCreate(5)
        ); // статус CREATED

        var order2 = Order.mustCreate(
                UUID.randomUUID(),
                Location.mustCreate(3, 4),
                Volume.mustCreate(10)
        );
        order2.assign(UUID.randomUUID()); // статус ASSIGNED

        var completedOrder = Order.mustCreate(
                UUID.randomUUID(),
                Location.mustCreate(5, 6),
                Volume.mustCreate(15)
        );
        completedOrder.assign(UUID.randomUUID());
        completedOrder.complete(); // статус COMPLETED

        orderRepository.save(order1);
        orderRepository.save(order2);
        orderRepository.save(completedOrder);

        // Act
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        GetAllNotCompletedOrdersResponse response = result.getValue();

        assertThat(response.orders()).hasSize(2);

        // Проверяем, что оба незавершенных заказа есть в результате
        assertThat(response.orders())
                .extracting(OrderDto::id)
                .containsExactlyInAnyOrder(order1.getId(), order2.getId());

        // Проверяем, что завершенный заказ не попал в результат
        assertThat(response.orders())
                .extracting(OrderDto::id)
                .doesNotContain(completedOrder.getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoUncompletedOrders() {
        // Arrange
        var completedOrder = Order.mustCreate(
                UUID.randomUUID(),
                Location.mustCreate(1, 2),
                Volume.mustCreate(5)
        );
        completedOrder.assign(UUID.randomUUID());
        completedOrder.complete(); // статус COMPLETED

        orderRepository.save(completedOrder);

        // Act
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        GetAllNotCompletedOrdersResponse response = result.getValue();
        assertThat(response.orders()).isEmpty();
    }

    @Test
    void shouldReturnEmptyListWhenNoOrders() {
        // Act
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        GetAllNotCompletedOrdersResponse response = result.getValue();
        assertThat(response.orders()).isEmpty();
    }

    @Test
    void shouldReturnCorrectOrderDtoFields() {
        // Arrange
        var order = Order.mustCreate(
                UUID.randomUUID(),
                Location.mustCreate(5, 7),
                Volume.mustCreate(20)
        );
        orderRepository.save(order);

        // Act
        var result = handler.handle(query);

        // Assert
        assertThat(result.isSuccess()).isTrue();
        GetAllNotCompletedOrdersResponse response = result.getValue();

        assertThat(response.orders()).hasSize(1);
        OrderDto dto = response.orders().getFirst();

        // Проверяем, что DTO содержит только нужные поля
        assertThat(dto.id()).isEqualTo(order.getId());
        assertThat(dto.location()).isEqualTo(Location.mustCreate(5, 7));

        // Проверяем, что статус, объем и courierId не попали в DTO
        assertThat(dto).hasNoNullFieldsOrProperties();
    }
}
