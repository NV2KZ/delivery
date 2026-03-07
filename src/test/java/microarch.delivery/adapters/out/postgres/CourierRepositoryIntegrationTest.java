package microarch.delivery.adapters.out.postgres;

import microarch.delivery.core.domain.model.courier.Courier;
import microarch.delivery.core.domain.model.kernel.Location;
import microarch.delivery.core.domain.model.kernel.Volume;
import microarch.delivery.core.domain.model.kernel.Speed;
import microarch.delivery.core.ports.CourierRepository;
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
class CourierRepositoryIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired
    CourierRepository repository;

    // Внедряем JPA репозиторий для очистки таблицы перед каждым тестом
    @Autowired
    CourierJpaRepository jpaRepository;

    @BeforeEach
    void tearDown() {
        // Очищаем таблицу couriers перед каждым тестом
        jpaRepository.deleteAll();
    }

    @Test
    void canSaveAndFindById() {
        // Arrange
        var courier = Courier.mustCreate("Ivan", Speed.mustCreate(1), Location.mustCreate(1, 2));

        // Act
        var saved = repository.save(courier);
        var loaded = repository.findById(saved.getId());

        // Assert
        assertThat(loaded).isPresent();
        assertThat(loaded.get().getId()).isEqualTo(courier.getId());
        assertThat(loaded.get().getName()).isEqualTo(courier.getName());
        assertThat(loaded.get().getStoragePlaces()).isNotEmpty();
    }

    @Test
    void returnsEmptyWhenCourierNotFound() {
        // Arrange
        var nonExistentId = UUID.randomUUID();

        // Act
        var result = repository.findById(nonExistentId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void findAllAvailableReturnsEmptyWhenNoCouriers() {
        // Act
        List<Courier> availableCouriers = repository.findAllAvailable();

        // Assert
        assertThat(availableCouriers).isEmpty();
    }

    @Test
    void findAllAvailableReturnsCourierWithoutOrders() {
        // Arrange
        var courier = Courier.mustCreate("Peter", Speed.mustCreate(5), Location.mustCreate(5, 3));
        // Курьер создан без заказов, значит он доступен

        repository.save(courier);

        // Act
        List<Courier> availableCouriers = repository.findAllAvailable();

        // Assert
        assertThat(availableCouriers).hasSize(1);
        assertThat(availableCouriers.get(0).getId()).isEqualTo(courier.getId());
        assertThat(availableCouriers.get(0).getName()).isEqualTo(courier.getName());
    }

    @Test
    void findAllAvailableDoesNotReturnCourierWithActiveOrder() {
        // Arrange
        var orderId = UUID.randomUUID();
        var courier = Courier.mustCreate("Alex", Speed.mustCreate(2), Location.mustCreate(4, 4));

        // Добавляем заказ в хранилище курьера.
        // Согласно логике в CourierJpaRepository: курьер НЕ доступен, если у него есть StoragePlace с orderId != null
        courier.takeOrder(orderId, Volume.mustCreate(10));

        repository.save(courier);

        // Act
        List<Courier> availableCouriers = repository.findAllAvailable();

        // Assert
        // Курьер с активным заказом не должен попасть в список доступных
        assertThat(availableCouriers).isEmpty();
    }

    @Test
    void findAllAvailableReturnsOnlyFreeCouriers() {
        // Arrange
        var orderId = UUID.randomUUID();

        var freeCourier = Courier.mustCreate("Free", Speed.mustCreate(2), Location.mustCreate(1, 1));

        var busyCourier = Courier.mustCreate("Busy", Speed.mustCreate(3), Location.mustCreate(2, 2));
        busyCourier.takeOrder(orderId, Volume.mustCreate(5));

        repository.save(freeCourier);
        repository.save(busyCourier);

        // Act
        List<Courier> availableCouriers = repository.findAllAvailable();

        // Assert
        assertThat(availableCouriers).hasSize(1);
        assertThat(availableCouriers.get(0).getId()).isEqualTo(freeCourier.getId());
        assertThat(availableCouriers.get(0).getName()).isEqualTo(freeCourier.getName());
    }
}