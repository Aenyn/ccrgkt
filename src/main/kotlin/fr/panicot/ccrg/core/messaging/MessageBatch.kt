package fr.panicot.ccrg.core.messaging

/**
 * Created by William on 11/02/2017.
 */
data class MessageBatch(val messages: List<Message>, val latestId: Long)