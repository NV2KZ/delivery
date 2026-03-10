package microarch.delivery.core.application.commands;

import libs.errs.Error;
import libs.errs.UnitResult;
import lombok.RequiredArgsConstructor;
import microarch.delivery.core.domain.services.OrderDispatcher;
import microarch.delivery.core.ports.CourierRepository;
import microarch.delivery.core.ports.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignOrderCommandHandlerImpl implements AssignOrderCommandHandler {

    private final OrderRepository orderRepository;
    private final CourierRepository courierRepository;
    private final OrderDispatcher orderDispatcher;

    @Override
    @Transactional
    public UnitResult<Error> handle(AssignOrderCommand command) {
        var orderOpt = orderRepository.findAnyCreated();
        if (orderOpt.isEmpty())
            // Если новых заказов нет, завершаем
            return UnitResult.success();

        var order = orderOpt.get();
        var availableCouriers = courierRepository.findAllAvailable();

        var assignedCourierResult = orderDispatcher.dispatch(order, availableCouriers);

        if (assignedCourierResult.isFailure())
            return UnitResult.failure(assignedCourierResult.getError());

        orderRepository.save(order);
        courierRepository.save(assignedCourierResult.getValue());

        return UnitResult.success();
    }
}
