package fr.panicot.ccrg.core.messaging

import java.time.LocalTime

/**
 * Created by William on 11/02/2017.
 */
data class Message(val id: Long, val timestamp: LocalTime, val author: String, val content: String)