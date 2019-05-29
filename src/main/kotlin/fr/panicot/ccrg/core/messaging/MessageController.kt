package fr.panicot.ccrg.core.messaging

import org.apache.commons.lang3.StringEscapeUtils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.servlet.http.HttpServletRequest

/**
 * Created by William on 11/02/2017.
 */
@RestController
class MessageController {
    private val SYSTEM_ANNOUNCEMENT = "System announcement"
    private val counter = AtomicLong()
    private val messages = ArrayList<Message>()
    private val users = HashMap<String, User>()
    private val kickingTimeouts = HashMap<String, Long>()


    @RequestMapping("/messages/longPoll", method = arrayOf(RequestMethod.GET))
    fun longPollMessages(@RequestParam(value = "latestId", defaultValue = "0") id: Long, @RequestParam(value = "user", required = false) user: String? = null): MessageBatch {
        val waitCount = AtomicLong(0L)
        var messagesSinceId = getMessagesSinceId(id, user)
        while (messagesSinceId.messages.isEmpty()) {
            Thread.sleep(100)
            messagesSinceId = getMessagesSinceId(id, user)
            if (waitCount.incrementAndGet() > 290) return messagesSinceId
        }
        return messagesSinceId
    }

    @RequestMapping("/messages/get", method = arrayOf(RequestMethod.GET))
    fun getMessages(@RequestParam(value = "latestId", defaultValue = "0") id: Long): MessageBatch {
        return getMessagesSinceId(id)
    }

    @RequestMapping("/messages/send", method = arrayOf(RequestMethod.POST))
    fun sendMessage(@RequestParam("author") author: String, @RequestParam("content") content: String, @RequestParam("author") recipient: String? = null) {
        val timestamp = LocalDateTime.now()
        val initialMessage = Message(counter.incrementAndGet(), timestamp.toLocalTime(), author, content, recipient)
        val processedMessage = processMessage(initialMessage)
        if (processedMessage.content.isNotBlank()) messages.add(processedMessage)
    }

    @RequestMapping("/users/get", method = arrayOf(RequestMethod.GET))
    fun getUsers(): List<User> {
        return getActiveUsers()
    }

    @RequestMapping("/users/me", method = arrayOf(RequestMethod.GET))
    fun getMe(request: HttpServletRequest): User {
        val username = StringEscapeUtils.escapeHtml4(request.remoteUser)
        return if (users.containsKey(username)) {
            users[username] ?: User(username, false, LocalDateTime.now())
        } else {
            val user = User(username, false, LocalDateTime.now())
            users.put(username, user)
            user
        }
    }

    @RequestMapping("/users/announce", method = arrayOf(RequestMethod.POST))
    fun announceArrival(@RequestParam("user") user: String, @RequestParam("isArrival") isArrival: Boolean) {
        val requestTime = LocalDateTime.now()
        updateUserLastSeen(user, requestTime)
        val announcement = user + if (isArrival) " vient de se connecter" else " vient de ragequit"
        val announcementMessage = Message(counter.incrementAndGet(), requestTime.toLocalTime(), SYSTEM_ANNOUNCEMENT, announcement)
        messages.add(announcementMessage)
    }

    @RequestMapping("/users/keepalive", method = arrayOf(RequestMethod.GET))
    fun keepAlive(@RequestParam("user") user: String): String {
        val requestTime = LocalDateTime.now()
        updateUserLastSeen(user, requestTime)
        return "ok"
    }

    @RequestMapping("/users/kick", method = arrayOf(RequestMethod.POST))
    fun kick(@RequestParam("userToKick") userToKick: String, @RequestParam("kickingUser") kickingUser: String): String {
        if (kickingTimeouts[kickingUser] ?: 0 <= System.currentTimeMillis()) {
            kickingTimeouts[kickingUser] = System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1L)
            doKick(userToKick)
        } else {
            doKick(kickingUser)
        }
        return "ok"
    }

    @RequestMapping("/users/setafk", method = arrayOf(RequestMethod.GET))
    fun setAfk(@RequestParam("user") user: String): String {
        setOrUnsetAfk(user, true)
        return "ok"
    }

    @RequestMapping("/users/unsetafk", method = arrayOf(RequestMethod.GET))
    fun unsetAfk(@RequestParam("user") user: String): String {
        if (isUserAfk(user)) {
            setOrUnsetAfk(user, false)
        }
        return "ok"
    }

    fun doKick(userToKick: String) {
        val user = users[userToKick]
        if (user != null) {
            messages.add(Message(counter.incrementAndGet(), LocalTime.now(), SYSTEM_ANNOUNCEMENT, "$userToKick vient de se faire kicker"))
            messages.add(Message(counter.incrementAndGet(), LocalTime.now(), SYSTEM_ANNOUNCEMENT, "Hahaha pates a la carbonarrache, excellent", userToKick, "kick"))
        }
    }

    fun setOrUnsetAfk(user: String, afk: Boolean) {
        val requestTime = LocalDateTime.now()
        updateUserLastSeen(user, requestTime, afk)
        val announcement = user + if (afk) " est maintenant afk" else " n'est plus afk"
        val afkMessage = Message(counter.incrementAndGet(), requestTime.toLocalTime(), SYSTEM_ANNOUNCEMENT, announcement)
        messages.add(afkMessage)

    }

    fun getMessagesSinceId(id: Long, user: String?): MessageBatch {
        return MessageBatch(messages.filter { message -> message.id > id }.filter { message -> (message.recipient == null) || (message.recipient == user) }, counter.get())
    }

    fun getMessagesSinceId(id: Long): MessageBatch {
        return getMessagesSinceId(id, null)
    }

    fun getActiveUsers(): List<User> {
        val requestTime = LocalDateTime.now()
        return users.filter { user -> user.value.lastSeen.isAfter(requestTime.minusMinutes(5L)) }.map { user -> user.value }.sortedBy { user -> user.name }
    }

    fun updateUserLastSeen(author: String, timestamp: LocalDateTime) {
        if (users.containsKey(author)) {
            users[author]?.lastSeen = timestamp
        } else {
            users[author] = User(author, false, timestamp)
        }
    }

    fun updateUserLastSeen(author: String, timestamp: LocalDateTime, afk: Boolean) {
        if (users.containsKey(author)) {
            users[author]?.lastSeen = timestamp
            users[author]?.afk = afk
        } else {
            users[author] = User(author, afk, timestamp)
        }
    }

    fun isUserAfk(user: String): Boolean {
        return users[user]?.afk ?: false
    }

    fun processMessage(message: Message): Message {
        return MessageProcessor(message)
                .escapeHtmlTags()
                .processLinks()
                .adjustTime(System.getProperty("TIME_DIFFERENCE", "0").toLong())
                .finalizeMessage()
    }
}