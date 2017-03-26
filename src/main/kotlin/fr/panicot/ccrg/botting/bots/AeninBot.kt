package fr.panicot.ccrg.botting.bots

import fr.panicot.ccrg.botting.EasyBot
import fr.panicot.ccrg.botting.util.SchedulableTask
import fr.panicot.ccrg.core.messaging.Message
import fr.panicot.ccrg.core.messaging.MessageController
import java.util.*

/**
 * Created by William on 18/03/2017.
 */
class AeninBot(messageController: MessageController, random: Random) : EasyBot(messageController, random) {

    private val BOT_NAME = "AeninBot"
    val javouList = Arrays.asList("OUAIS JAVOU", "JAVOU")
    val hahaList = Arrays.asList("haha", "haha nice", "^^ nice")

    override fun executeOnNewMessage(message: Message) {
        if(message.author == "Aenin" || !willAct(100)) {
            return
        }
        else if (message.content.contains("<a target")) {
            messageController.sendMessage(BOT_NAME, getRandomHaha())
        } else {
            messageController.sendMessage(BOT_NAME, getRandomJavou())
        }
    }

    override fun getTasksToSchedule(): Collection<SchedulableTask> {
        return Collections.emptyList()
    }

    private fun getRandomJavou() : String {
        return javouList[random.nextInt(javouList.size)]
    }

    private fun getRandomHaha() : String {
        return hahaList[random.nextInt(hahaList.size)]
    }

    private fun willAct(chances : Int) : Boolean {
        return random.nextInt(chances) == 0
    }

}