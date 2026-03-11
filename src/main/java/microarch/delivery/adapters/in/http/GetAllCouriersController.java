package microarch.delivery.adapters.in.http;

import lombok.RequiredArgsConstructor;
import microarch.delivery.adapters.in.http.api.DeliveryApi;
import microarch.delivery.adapters.in.http.mapper.CourierMapper;
import microarch.delivery.adapters.in.http.model.Courier;
import microarch.delivery.core.application.queries.GetAllCouriersQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GetAllCouriersController implements DeliveryApi {

    private final GetAllCouriersQueryHandler getAllCouriersQueryHandler;

    @Override
    public ResponseEntity<List<Courier>> getCouriers() {

        var result = this.getAllCouriersQueryHandler.handle();
        if (result.isFailure())
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // Формируем ответ
        var response = result.getValue().couriers().stream()
                .map(CourierMapper::mapCourierDtoToView)
                .toList();

        return ResponseEntity.ok(response);
    }

}
