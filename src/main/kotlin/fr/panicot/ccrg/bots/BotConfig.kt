package fr.panicot.ccrg.bots

import fr.panicot.ccrg.core.messaging.MessageController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

/**
 * Created by William on 16/03/2017.
 */
@Configuration
open class BotConfig {
    @Autowired
    var messageController: MessageController? = null

    @Bean
    open fun startBots(): Boolean {
        var botList: List<Bot> = Arrays.asList(Attila(messageController!!))
        botList.forEach(Bot::start)
        return true
    }

}