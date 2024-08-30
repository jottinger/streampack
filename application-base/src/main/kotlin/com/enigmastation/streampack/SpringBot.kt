/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack

import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.routerMessage
import com.enigmastation.streampack.whiteboard.service.Router
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.support.ApplicationObjectSupport
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableScheduling
class SpringBot : CommandLineRunner, ApplicationObjectSupport() {
    @Bean fun restTemplate(): RestTemplate = RestTemplate()

    override fun run(vararg args: String?) {
        val router = applicationContext!!.getBean(Router::class.java)
        router.dispatch(
            routerMessage {
                content = ".init."
                messageSource = MessageSource.INTERNAL
            }
        )
    }
}

fun main(args: Array<String>) {
    runApplication<SpringBot>(*args)
}
