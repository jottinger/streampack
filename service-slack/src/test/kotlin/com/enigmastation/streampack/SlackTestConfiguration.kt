package com.enigmastation.streampack

import com.enigmastation.streampack.slack.service.SlackService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@SpringBootApplication
class SlackTestConfiguration{
    @Bean fun commandLineRunner(ctx:ApplicationContext, service: SlackService): CommandLineRunner {
        return object:CommandLineRunner{
            override fun run(vararg args: String?) {
                println("Hello, world: $service")
                println("configuration: ${service.configuration}")
                service.join()
            }
        }
    }

}

fun main(args: Array<String>) {
    runApplication<SlackTestConfiguration>(*args)
}
