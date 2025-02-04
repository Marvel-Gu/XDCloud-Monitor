package com.example.config;

import com.example.task.MonitorJobBean;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class QuartzConfiguration {

    //定时任务实现
    @Bean
    public JobDetail jobDetailFactoryBean() {
        return JobBuilder.newJob(MonitorJobBean.class)
                .withIdentity("monitor-task")
                .storeDurably() //开启持久化存储
                .build();
    }

    //定时任务触发器
    @Bean
    public Trigger cronTriggerFactoryBean(JobDetail detail) {
        CronScheduleBuilder cron = CronScheduleBuilder.cronSchedule("*/10 * * * * ?");
        return TriggerBuilder.newTrigger()
                .forJob(detail)
                .withIdentity("monitor-trigger")
                .withSchedule(cron)
                .build();
    }
}
