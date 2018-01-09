package fr.panicot.ccrg.botting.bots

import fr.panicot.ccrg.botting.Bot
import fr.panicot.ccrg.core.messaging.MessageController
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import java.util.*

/**
 * Created by William on 16/03/2017.
 */
class AttilaBot(messageController: MessageController, random: Random): Bot(messageController, random) {
    val scheduler = ThreadPoolTaskScheduler()
    val quoteList = Arrays.asList("YAAAAAAARGH !",
            "LA OU JE PASSE L'HERBE NE REPOUSSE JAMAIS !",
            "PLACE AU GRAND ATTILA !",
            "JE SUIS LE FLEAU DE DIEU !",
            "AAAAAAAARGH !",
            "COUREZ POUR VOS VIES !",
            "JE SUIS ATTILA LE HUN !")

    fun randomPick() : String = quoteList.get(random.nextInt(quoteList.size))

    override fun start() {
        scheduler.initialize()
        scheduler.schedule(Runnable{messageController.announceArrival("ATTILABOT", true)}, CronTrigger("0 11 10 * * *"))
        scheduler.schedule(Runnable{messageController.sendMessage("ATTILABOT", randomPick())}, CronTrigger("1-59/4 11 10 * * *"))
        scheduler.schedule(Runnable{messageController.announceArrival("ATTILABOT", false)}, CronTrigger("0 12 10 * * *"))
    }
}
