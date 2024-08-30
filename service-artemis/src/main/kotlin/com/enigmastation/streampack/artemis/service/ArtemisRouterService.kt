/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.artemis.service

import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterService
import jakarta.jms.JMSException
import jakarta.jms.Message
import jakarta.jms.MessageListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.listener.DefaultMessageListenerContainer
import org.springframework.stereotype.Service

@Service
class ArtemisRouterService : RouterService() {
    @Autowired lateinit var jmsTemplate: JmsTemplate

    override fun canHandle(message: RouterMessage): Boolean = false

    @Bean
    fun messageListenerContainer(): DefaultMessageListenerContainer {
        var container = DefaultMessageListenerContainer()
        container.connectionFactory = jmsTemplate.connectionFactory
        container.destinationName = "myQueue"
        container.messageListener =
            object : MessageListener {
                override fun onMessage(message: Message) {
                    // need to try to figure out how to decode messages properly: just JSON form for
                    // RouterMessage?
                    try {
                        println("Received message: " + message.getBody(String::class.java))
                    } catch (e: JMSException) {
                        e.printStackTrace()
                    }
                }
            }

        return container
    }
}
