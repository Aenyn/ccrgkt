package fr.panicot.ccrg.core.messaging

/**
 * Created by William on 11/02/2017.
 */
data class Message(val id: Long, val timestamp: String, val author: String, val content: String)