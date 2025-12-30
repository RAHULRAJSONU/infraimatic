package com.ezyinfra.product.checkpost.identity.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(
            ScheduledTaskRegistrar registrar) {

        registrar.setScheduler(
                Executors.newScheduledThreadPool(
                        1,
                        Thread.ofVirtual()
                              .name("tenant-scheduler-", 0)
                              .factory()
                )
        );
    }
}
