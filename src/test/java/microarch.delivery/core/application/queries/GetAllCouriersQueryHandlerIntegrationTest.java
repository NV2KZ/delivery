package microarch.delivery.core.application.queries;

import microarch.delivery.adapters.out.postgres.CourierJpaRepository;
import microarch.delivery.adapters.out.postgres.PostgresIntegrationTestBase;
import microarch.delivery.core.application.queries.dto.CourierDto;
import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Speed;
import microarch.delivery.core.ports.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GetAllCouriersQueryHandlerIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    private GetAllCouriersQueryHandler handler;

    @Autowired
    private CourierRepository courierRepository;

    @Autowired
    private CourierJpaRepository jpaRepository;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    @Test
    void shouldReturnAllCouriers() {
        // Arrange
        var courier1 = Courier.mustCreate(
                "Иван Петров",
                Speed.mustCreate(2),
                Location.mustCreate(3, 4)
        );

        var courier2 = Courier.mustCreate(
                "Петр Иванов",
                Speed.mustCreate(1),
                Location.mustCreate(7, 8)
        );

        courierRepository.save(courier1);
        courierRepository.save(courier2);

        // Act
        var result = handler.handle();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        var response = result.getValue();

        assertThat(response.couriers()).hasSize(2);

        // Проверяем первого курьера
        CourierDto dto1 = findDtoById(response.couriers(), courier1.getId());
        assertThat(dto1).isNotNull();
        assertThat(dto1.name()).isEqualTo("Иван Петров");
        assertThat(dto1.location()).isEqualTo(Location.mustCreate(3, 4));

        // Проверяем второго курьера
        CourierDto dto2 = findDtoById(response.couriers(), courier2.getId());
        assertThat(dto2).isNotNull();
        assertThat(dto2.name()).isEqualTo("Петр Иванов");
        assertThat(dto2.location()).isEqualTo(Location.mustCreate(7, 8));
    }

    @Test
    void shouldReturnEmptyListWhenNoCouriers() {
        // Act
        var result = handler.handle();

        // Assert
        assertThat(result.isSuccess()).isTrue();
        GetAllCouriersResponse response = result.getValue();
        assertThat(response.couriers()).isEmpty();
    }

    // Вспомогательный метод для поиска DTO по ID
    private CourierDto findDtoById(List<CourierDto> dtos, java.util.UUID id) {
        return dtos.stream()
                .filter(dto -> dto.id().equals(id))
                .findFirst()
                .orElse(null);
    }
}