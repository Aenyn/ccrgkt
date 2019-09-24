package fr.panicot.ccrg.botting.bots

import fr.panicot.ccrg.botting.Bot
import fr.panicot.ccrg.core.messaging.MessageController
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by William on 16/03/2017.
 */
class AttilaBot(messageController: MessageController, random: Random): Bot(messageController, random) {
    private val scheduler = ThreadPoolTaskScheduler()
    private val armySize = AtomicInteger(0)

    override fun start() {
        scheduler.initialize()
        scheduler.schedule(Runnable{messageController.announceArrival("ATTILABOT", true)}, CronTrigger("0 11 9 * * *"))
        scheduler.schedule(Runnable{
            messageController.announceArrival("HUNBOT${armySize.getAndIncrement()}", true)
        }, CronTrigger("1-59/8 11 9 * * *"))
        scheduler.schedule(Runnable{messageController.announceArrival("ATTILABOT", false)}, CronTrigger("0 12 9 * * *"))
        scheduler.schedule(Runnable{for (i in 0..armySize.get()) {
            messageController.announceArrival("HUNBOT$i", false)
        }}, CronTrigger("0 12 9 * * *"))
    }
}
