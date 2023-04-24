package net.musma.hhi.middleware.mewp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AppConfig {
    @Bean(name="defaultTaskExecutor",destroyMethod="shutdown")
    public ThreadPoolTaskExecutor defaultTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(200);
        executor.setMaxPoolSize(200);
//        executor.setQueueCapacity(300); // Queue 사이즈
        executor.initialize();
        return executor;
    }

}
