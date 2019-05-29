package fr.panicot.ccrg.botting

import fr.panicot.ccrg.botting.bots.AeninBot
import fr.panicot.ccrg.botting.bots.AttilaBot
import fr.panicot.ccrg.botting.bots.ClausseBot
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
        val random = Random(System.nanoTime())
        val botList: List<Bot> = Arrays.asList(AttilaBot(messageController!!, random), ClausseBot(messageController!!, random), AeninBot(messageController!!, random))
        botList.forEach(Bot::start)
        return true
    }

}