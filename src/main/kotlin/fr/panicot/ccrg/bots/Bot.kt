package fr.panicot.ccrg.bots

import fr.panicot.ccrg.core.messaging.MessageController

/**
 * Created by William on 16/03/2017.
 */
abstract class Bot(val messageController: MessageController) {
    abstract fun start(): Unit
}