package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.CreateOrderApi;
import microarch.delivery.adapters.in.http.model.CreateOrderResponse;
import microarch.delivery.core.application.commands.CreateOrderCommand;
import microarch.delivery.core.application.commands.CreateOrderCommandHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

//TODO Далее в курсе мы будем создавать заказ, обрабатывая сообщение из Kafka от микросервиса Basket,
// но пока этот функционал у нас не реализован
@RestController
@RequiredArgsConstructor
public class CreateOrderController implements CreateOrderApi {

    private final CreateOrderCommandHandler createOrderCommandHandler;

    @Override
    public ResponseEntity<CreateOrderResponse> createOrder() {
        // Формируем команду
        var createCommandResult = CreateOrderCommand.create(
                UUID.randomUUID(),
                "Россия", "Москва", "Айтишная", "1", "2",5
        );

        if (createCommandResult.isFailure()) {
            return ResponseEntity.badRequest().build();
        }
        var command = createCommandResult.getValue();

        // Обрабатываем команду
        var handleResult = this.createOrderCommandHandler.handle(command);
        if (handleResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Формируем ответ
        var response = new CreateOrderResponse();
        response.setOrderId(handleResult.getValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
