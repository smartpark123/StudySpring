package com.StudySpring.infra.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
// 메인 로직과 이벤트를 분리하기 위한 목적

    @Override
    public Executor getAsyncExecutor(){
        //쓰레드 풀 생성
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int processors = Runtime.getRuntime().availableProcessors();
        log.info("processor count{}", processors);
        executor.setCorePoolSize(processors);
        executor.setMaxPoolSize(processors * 2);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();
        return executor;
    }
}
