package microarch.delivery.adapters.in.kafka;

import lombok.RequiredArgsConstructor;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import queues.basket.BasketEventsProto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasketEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(BasketEventsConsumer.class);
    private final CreateOrderCommandHandler createOrderCommandHandler;

    @KafkaListener(topics = "${app.kafka.baskets-events-topic}")
    public void listen(byte[] message) {
        try {
            var event = BasketEventsProto.BasketConfirmedIntegrationEvent.parseFrom(message);

            // Создаем команду
            var createCommandResult = CreateOrderCommand.create(
                    UUID.fromString(event.getBasketId()),
                    event.getAddress().getCountry(),
                    event.getAddress().getCity(),
                    event.getAddress().getStreet(),
                    event.getAddress().getHouse(),
                    event.getAddress().getApartment(),
                    event.getVolume()
            );
            if (createCommandResult.isFailure()) {
                throw new RuntimeException("Invalid command: " + createCommandResult.getError());
            }
            var command = createCommandResult.getValue();

            // Обрабатываем команду
            var handleCommandResult = createOrderCommandHandler.handle(command);
            if (handleCommandResult.isFailure()) {
                throw new RuntimeException("Failed to handle command: " + handleCommandResult.getError());
            }
        } catch (com.google.protobuf.InvalidProtocolBufferException ex) {
            throw new RuntimeException("Failed to parse protobuf message", ex);
        }
    }
}
