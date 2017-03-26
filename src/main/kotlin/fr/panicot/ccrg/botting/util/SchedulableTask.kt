package fr.panicot.ccrg.botting.util

import org.springframework.scheduling.support.CronTrigger

/**
 * Created by William on 18/03/2017.
 */
data class SchedulableTask(val task: Runnable, val schedule: CronTrigger)