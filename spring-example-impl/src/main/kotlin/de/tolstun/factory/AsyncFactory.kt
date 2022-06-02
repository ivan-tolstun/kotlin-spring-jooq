package de.tolstun.factory

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor


@Configuration
open class AsyncFactory {


    @Bean
    open fun taskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 3
        executor.setQueueCapacity(100)
        executor.threadNamePrefix = "async-spring-example-"
        executor.initialize()
        return executor
    }


}