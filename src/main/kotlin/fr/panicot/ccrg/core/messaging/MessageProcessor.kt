package fr.panicot.ccrg.core.messaging

import org.apache.commons.lang3.StringEscapeUtils

/**
 * Created by William on 13/02/2017.
 */
class MessageProcessor(val message: Message) {
    fun processLinks(): MessageProcessor {
        val messageContent = message.content
        val messageSplit = messageContent.split(" ")
        val urlRegex = Regex("(http(s)?:\\/\\/.)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&/=()]*)")
        val messageProcessed = Message(message.id,message.timestamp,message.author,
                messageSplit.map { part ->
                    if(part.matches(urlRegex)){
                        val partWithHttp = if (part.startsWith("http")) part else "http://$part"
                        "<a target=\"_blank\" href=$partWithHttp>$part</a>"
                    }
                    else part
                }.joinToString(" "))
        return MessageProcessor(messageProcessed)
    }

    fun escapeHtmlTags(): MessageProcessor {
        val messageProcessed = Message(message.id,message.timestamp,
                StringEscapeUtils.escapeHtml4(message.author),
                message.content.replace("<","&lt;").replace(">","&gt;"))
        return MessageProcessor(messageProcessed)
    }

    fun adjustTime(timeDifference: Long): MessageProcessor {
        val messageProcessed = Message(message.id, message.timestamp.plusHours(timeDifference), message.author, message.content)
        return MessageProcessor(messageProcessed)

    }

    fun finalizeMessage(): Message { return message }
}