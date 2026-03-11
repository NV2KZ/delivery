package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.DeliveryApi;
import microarch.delivery.adapters.in.http.mapper.OrderMapper;
import microarch.delivery.adapters.in.http.model.Order;
import microarch.delivery.core.application.queries.GetAllNotCompletedOrdersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetAllNotCompletedOrdersController implements DeliveryApi {
    private final GetAllNotCompletedOrdersQueryHandler getAllNotCompletedOrdersQueryHandler;
    @Override
    public ResponseEntity<List<Order>> getOrders() {

        var result = this.getAllNotCompletedOrdersQueryHandler.handle();
        if (result.isFailure())
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // Формируем ответ
        var response = result.getValue().orders().stream()
                .map(OrderMapper::mapOrderDtoToView)
                .toList();

        return ResponseEntity.ok(response);
    }
}
