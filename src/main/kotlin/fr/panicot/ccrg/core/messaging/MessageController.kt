package fr.panicot.ccrg.core.messaging

import org.apache.commons.lang3.StringEscapeUtils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import javax.servlet.http.HttpServletRequest

/**
 * Created by William on 11/02/2017.
 */
@RestController
class MessageController {
    val SYSTEM_ANNOUNCEMENT = "System announcement"
    val counter = AtomicLong()
    val messages = ArrayList<Message>()
    val users = HashMap<String, User>()


    @RequestMapping("/messages/longPoll", method = arrayOf(RequestMethod.GET))
    fun longPollMessages(@RequestParam(value = "latestId", defaultValue = "0") id: Long): MessageBatch {
        val waitCount = AtomicLong(0L)
        var messagesSinceId = getMessagesSinceId(id)
        while (messagesSinceId.messages.isEmpty()) {
            Thread.sleep(100)
            messagesSinceId = getMessagesSinceId(id)
            if(waitCount.incrementAndGet() > 290) return messagesSinceId
        }
        return messagesSinceId
    }

    @RequestMapping("/messages/get", method = arrayOf(RequestMethod.GET))
    fun getMessages(@RequestParam(value = "latestId", defaultValue = "0") id: Long): MessageBatch {
        return getMessagesSinceId(id)
    }

    @RequestMapping("/messages/send", method = arrayOf(RequestMethod.POST))
    fun sendMessage(@RequestParam("author") author: String, @RequestParam("content") content: String): Unit {
        val timestamp = LocalDateTime.now()
        val initialMessage = Message(counter.incrementAndGet(), timestamp.toLocalTime(), author, content)
        val processedMessage = processMessage(initialMessage)
        if(processedMessage.content.isNotBlank()) messages.add(processedMessage)
    }

    @RequestMapping("/users/get", method = arrayOf(RequestMethod.GET))
    fun getUsers(): List<User> {
        return getActiveUsers()
    }

    @RequestMapping("/users/me", method = arrayOf(RequestMethod.GET))
    fun getMe(request: HttpServletRequest): User {
        val username = StringEscapeUtils.escapeHtml4(request.remoteUser)
        if (users.containsKey(username)) {
            return users[username]?:User(username, false, LocalDateTime.now())
        } else {
            val user = User(username, false, LocalDateTime.now())
            users.put(username, user)
            return user
        }
    }

    @RequestMapping("/users/announce", method = arrayOf(RequestMethod.POST))
    fun announceArrival(@RequestParam("user") user: String, @RequestParam("isArrival") isArrival: Boolean): Unit {
        val requestTime = LocalDateTime.now()
        updateUserLastSeen(user, requestTime)
        val announcement = user + if(isArrival) " vient de se connecter" else " vient de ragequit"
        val announcementMessage = Message(counter.incrementAndGet(), requestTime.toLocalTime(), SYSTEM_ANNOUNCEMENT, announcement)
        messages.add(announcementMessage)
    }

    @RequestMapping("/users/keepalive", method = arrayOf(RequestMethod.GET))
    fun keepAlive(@RequestParam("user") user: String): String {
        val requestTime = LocalDateTime.now()
        updateUserLastSeen(user, requestTime)
        return "ok"
    }

    @RequestMapping("/users/setafk", method = arrayOf(RequestMethod.GET))
    fun setAfk(@RequestParam("user") user: String): String {
        val requestTime = LocalDateTime.now()
        updateUserLastSeen(user, requestTime, true)
        val announcement = user + " est maintenant afk"
        val afkMessage = Message(counter.incrementAndGet(), requestTime.toLocalTime(), SYSTEM_ANNOUNCEMENT, announcement)
        messages.add(afkMessage)
        return "ok"
    }

    @RequestMapping("/users/unsetafk", method = arrayOf(RequestMethod.GET))
    fun unsetAfk(@RequestParam("user") user: String): String {
        val requestTime = LocalDateTime.now()
        updateUserLastSeen(user, requestTime, false)
        val announcement = user + " n'est plus afk"
        val afkMessage = Message(counter.incrementAndGet(), requestTime.toLocalTime(), SYSTEM_ANNOUNCEMENT, announcement)
        messages.add(afkMessage)
        return "ok"
    }

    fun getMessagesSinceId(id: Long): MessageBatch {
        return MessageBatch(messages.filter { message -> message.id > id }, counter.get())
    }

    fun getActiveUsers(): List<User> {
        val requestTime = LocalDateTime.now()
        return users.filter { user -> user.value.lastSeen.isAfter(requestTime.minusMinutes(5L)) }.map { user -> user.value }
    }

    fun updateUserLastSeen(author: String, timestamp: LocalDateTime): Unit {
        val escapedAuthor = author
        if (users.containsKey(escapedAuthor)) {
            users[escapedAuthor]?.lastSeen = timestamp
        } else {
            users.put(escapedAuthor, User(escapedAuthor, false, timestamp))
        }
    }

    fun updateUserLastSeen(author: String, timestamp: LocalDateTime, afk: Boolean): Unit {
        val escapedAuthor = author
        if (users.containsKey(escapedAuthor)) {
            users[escapedAuthor]?.lastSeen = timestamp
            users[escapedAuthor]?.afk = afk
        } else {
            users.put(escapedAuthor, User(escapedAuthor, afk, timestamp))
        }
    }

    fun processMessage(message: Message): Message {
        return MessageProcessor(message)
                .escapeHtmlTags()
                .processLinks()
                .adjustTime(System.getProperty("TIME_DIFFERENCE", "0").toLong())
                .finalizeMessage()
    }
}