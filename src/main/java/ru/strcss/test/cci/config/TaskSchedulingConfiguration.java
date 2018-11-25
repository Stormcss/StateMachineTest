package ru.strcss.test.cci.config;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import ru.strcss.test.cci.scheduler.UnapprovedRqJobObserver;

import static org.quartz.CronScheduleBuilder.cronSchedule;

/**
 * Created by Stormcss
 * Date: 25.11.2018
 */
@Configuration
public class TaskSchedulingConfiguration {
    @Value("${unapproved.interval:0/10 * * * * ?}")
    private String interval;

    @Bean
    public Trigger sampleJobTrigger(JobDetail jobDetail) {
        return TriggerBuilder.newTrigger().withIdentity("cronUnapprovedTrigger")
                .withSchedule(cronSchedule(interval))
                .forJob(jobDetail)
                .build();
    }

    @Bean
    public JobDetailFactoryBean sampleJobDetailFactory() {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(UnapprovedRqJobObserver.class);
        jobDetailFactory.setName("processReportJob");
        jobDetailFactory.setDurability(true);
        return jobDetailFactory;
    }
}
