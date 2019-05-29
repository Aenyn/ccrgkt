package fr.panicot.ccrg.botting.bots

import fr.panicot.ccrg.botting.Bot
import fr.panicot.ccrg.core.messaging.MessageController
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import java.time.LocalDate
import java.util.*

/**
 * Created by William on 16/03/2017.
 */
class ClausseBot(messageController: MessageController, random: Random): Bot(messageController, random) {
    val scheduler = ThreadPoolTaskScheduler()
    val averageMinutesBetweenInterventions = 20
    var currentName = "ClausseBot"
    var currentStatus = ClausseStatus.DECO
    var lastConnectionDay : LocalDate? = null

    override fun start() {
        scheduler.initialize()
        scheduler.schedule(Runnable{changeState()}, CronTrigger("0 * 9-16 * * *"))
    }

    fun changeState() {
        if (currentStatus != ClausseStatus.DECO) messageController.keepAlive(currentName)
        val willIntervene = random.nextInt(averageMinutesBetweenInterventions) == 0
        if (!willIntervene) {

        } else if (currentStatus == ClausseStatus.DECO) {
            val connectionMessage = if  (hasConnectedToday()) "re" else "-"
            currentStatus = ClausseStatus.PRESENT
            messageController.announceArrival(currentName, true)
            Thread.sleep(500L + random.nextInt(2000).toLong())
            messageController.sendMessage(currentName, connectionMessage)
        } else if (currentStatus == ClausseStatus.AFK) {
            val toDeco = random.nextBoolean()
            if (toDeco) {
                messageController.announceArrival(currentName, false)
                currentStatus = ClausseStatus.DECO
            } else {
                currentStatus = ClausseStatus.PRESENT
                messageController.unsetAfk(currentName)
                messageController.sendMessage(currentName, "re")
            }
        } else {
            val toDeco = random.nextBoolean()
            currentStatus = if (toDeco) {
                messageController.sendMessage(currentName, "++")
                Thread.sleep(500L + random.nextInt(2000).toLong())
                messageController.announceArrival(currentName, false)
                ClausseStatus.DECO
            } else {
                messageController.setAfk(currentName)
                Thread.sleep(500L + random.nextInt(2000).toLong())
                ClausseStatus.AFK
            }
        }
    }

    fun hasConnectedToday() : Boolean {
        val previousConnectionDay = lastConnectionDay
        lastConnectionDay = LocalDate.now()
        return previousConnectionDay == lastConnectionDay
    }

    enum class ClausseStatus {
        AFK, PRESENT, DECO
    }
}