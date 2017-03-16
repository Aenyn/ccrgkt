package fr.panicot.ccrg.bots

import fr.panicot.ccrg.core.messaging.MessageController
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import java.util.*

/**
 * Created by William on 16/03/2017.
 */
class Attila(messageController: MessageController): Bot(messageController) {
    val scheduler = ThreadPoolTaskScheduler()
    val random = Random(System.nanoTime())
    val quoteList = Arrays.asList("YAAAAAAARGH !",
            "LA OU JE PASSE L'HERBE NE REPOUSSE JAMAIS !",
            "PLACE AU GRAND ATTILA !",
            "JE SUIS LE FLEAU DE DIEU !",
            "AAAAAAAARGH !",
            "COURREZ POUR VOS VIES !",
            "JE SUIS ATTILA LE HUN !")

    fun randomPick() : String = quoteList.get(random.nextInt(quoteList.size))

    override fun start() {
        scheduler.initialize()
        scheduler.schedule(Runnable{messageController.announceArrival("ATTILA", true)}, CronTrigger("0 11 11 * * *"))
        scheduler.schedule(Runnable{messageController.sendMessage("ATTILA", randomPick())}, CronTrigger("1-59/4 11 11 * * *"))
        scheduler.schedule(Runnable{messageController.announceArrival("ATTILA", false)}, CronTrigger("0 11 11 * * *"))
    }
}