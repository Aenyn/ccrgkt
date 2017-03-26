package fr.panicot.ccrg.botting

import fr.panicot.ccrg.botting.util.SchedulableTask
import fr.panicot.ccrg.core.messaging.Message
import fr.panicot.ccrg.core.messaging.MessageController
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import java.util.*
import java.util.concurrent.Executors

/**
 * Created by William on 18/03/2017.
 */
abstract class EasyBot(messageController: MessageController, random: Random) : Bot(messageController, random) {
    private val scheduler = ThreadPoolTaskScheduler()
    private val executor = Executors.newSingleThreadExecutor()
    private var latestId = 0L

    override fun start() {
        scheduler.initialize()
        getTasksToSchedule().forEach { schedulableTask -> scheduler.schedule(schedulableTask.task, schedulableTask.schedule) }
        executor.submit { recursivePollExecute() }
    }

    private fun recursivePollExecute() : Unit {
        val messageBatch = messageController.longPollMessages(this.latestId)
        this.latestId = messageBatch.latestId
        messageBatch.messages.forEach { message -> executeOnNewMessage(message) }
        executor.submit { recursivePollExecute() }
    }

    abstract fun getTasksToSchedule() : Collection<SchedulableTask>
    abstract fun executeOnNewMessage(message: Message) : Unit

}