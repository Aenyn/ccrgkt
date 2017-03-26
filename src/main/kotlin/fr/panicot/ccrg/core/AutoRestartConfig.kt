package fr.panicot.ccrg.core

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger

/**
 * Created by William on 26/03/2017.
 */
@Configuration
open class AutoRestartConfig {

    val executor = ThreadPoolTaskScheduler()

    @Bean
    open fun scheduleRestart(): Boolean {
        executor.initialize()
        executor.schedule(Runnable{System.exit(0)}, CronTrigger("0 0 3 * * *"))
        return true
    }
}