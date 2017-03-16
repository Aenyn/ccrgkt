package fr.panicot.ccrg.bots

import fr.panicot.ccrg.core.messaging.MessageController
import java.util.*

/**
 * Created by William on 16/03/2017.
 */
abstract class Bot(val messageController: MessageController, val random: Random) {
    abstract fun start(): Unit
}