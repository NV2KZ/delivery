package config;

import microarch.delivery.core.application.jobs.AssignOrdersJob;
import microarch.delivery.core.application.jobs.MoveCouriersJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail assignOrdersJobDetail() {
        return JobBuilder.newJob(AssignOrdersJob.class)
                .withIdentity("assignOrdersJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger assignOrdersTrigger(JobDetail assignOrdersJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(assignOrdersJobDetail)
                .withIdentity("assignOrdersTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(1)
                        .repeatForever())
                .build();
    }

    @Bean
    public JobDetail moveCouriersJobDetail() {
        return JobBuilder.newJob(MoveCouriersJob.class)
                .withIdentity("moveCouriersJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger moveCouriersTrigger(JobDetail moveCouriersJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(moveCouriersJobDetail)
                .withIdentity("moveCouriersTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(1)
                        .repeatForever())
                .build();
    }
}