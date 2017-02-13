package fr.panicot.ccrg.core.messaging

import org.apache.commons.lang3.StringEscapeUtils

/**
 * Created by William on 13/02/2017.
 */
class MessageProcessor(val message: Message) {
    fun processLinks(): MessageProcessor {
        val messageContent = message.content
        val messageSplit = messageContent.split(" ")
        val messageProcessed = Message(message.id,message.timestamp,message.author,
                messageSplit.map { part -> if(part.matches(Regex("([\\w|\\d]+\\..+)"))) "<a href=$part>$part</a>" else part}.joinToString(" "))
        return MessageProcessor(messageProcessed)
    }

    fun escapeHtmlTags(): MessageProcessor {
        val messageContent = message.content
        val messageProcessed = Message(message.id,message.timestamp,message.author,
                StringEscapeUtils.escapeHtml4(messageContent))
        return MessageProcessor(messageProcessed)
    }

    fun finalizeMessage(): Message { return message }
}