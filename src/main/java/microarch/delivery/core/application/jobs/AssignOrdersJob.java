package microarch.delivery.core.application.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import microarch.delivery.core.application.commands.AssignOrderCommandHandler;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssignOrdersJob implements Job {
    private final AssignOrderCommandHandler handler;

    @Override
    public void execute(JobExecutionContext context) {
        handler.handle();
    }
}
