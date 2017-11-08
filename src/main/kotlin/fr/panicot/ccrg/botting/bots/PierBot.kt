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
class PierBot(messageController: MessageController, random: Random): Bot(messageController, random) {
    val scheduler = ThreadPoolTaskScheduler()
    val nameList = Arrays.asList("Pire", "Pier", "Pyotr")
    val averageMinutesBetweenInterventions = 20
    var currentName = getRandomName()
    var currentStatus = PierStatus.DECO
    var lastConnectionDay : LocalDate? = null

    override fun start() {
        scheduler.initialize()
        scheduler.schedule(Runnable{changeState()}, CronTrigger("0 * 9-16 * * *"))
    }

    fun changeState() {
        if (currentStatus != PierStatus.DECO) messageController.keepAlive(currentName)
        val willIntervene = random.nextInt(averageMinutesBetweenInterventions) == 0
        if (!willIntervene) {

        } else if (currentStatus == PierStatus.DECO) {
            val connectionMessage = if  (hasConnectedToday()) "re" else "plop"
            currentStatus = PierStatus.PRESENT
            currentName = getRandomName()
            messageController.announceArrival(currentName, true)
            Thread.sleep(500L + random.nextInt(2000).toLong())
            messageController.sendMessage(currentName, connectionMessage)
        } else if (currentStatus == PierStatus.AFK) {
            val toDeco = random.nextBoolean()
            if (toDeco) {
                messageController.announceArrival(currentName, false)
                currentStatus = PierStatus.DECO
            } else {
                currentStatus = PierStatus.PRESENT
                messageController.unsetAfk(currentName)
                messageController.sendMessage(currentName, "re")
            }
        } else {
            val toDeco = random.nextBoolean()
            if (toDeco) {
                messageController.sendMessage(currentName, "rq")
                Thread.sleep(500L + random.nextInt(2000).toLong())
                messageController.announceArrival(currentName, false)
                currentStatus = PierStatus.DECO
            } else {
                messageController.sendMessage(currentName, "afk")
                messageController.setAfk(currentName)
                Thread.sleep(500L + random.nextInt(2000).toLong())
                currentStatus = PierStatus.AFK
            }
        }
    }

    fun hasConnectedToday() : Boolean {
        val previousConnectionDay = lastConnectionDay
        lastConnectionDay = LocalDate.now()
        return previousConnectionDay == lastConnectionDay
    }

    fun getRandomName() : String {
        return nameList[random.nextInt(nameList.size)] + "Bot"
    }

    enum class PierStatus {
        AFK, PRESENT, DECO
    }
}