package microarch.delivery.core.application.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.commands.MoveCourierCommand;
import microarch.delivery.core.application.commands.MoveCourierCommandHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MoveCouriersJob implements Job {

    private final MoveCourierCommandHandler handler;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Start move");
        handler.handle(MoveCourierCommand.create().getValue());
        log.info("Stop move");
    }
}
