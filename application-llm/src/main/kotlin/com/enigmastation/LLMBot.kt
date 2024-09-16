/* Joseph B. Ottinger (C)2024 */
package com.enigmastation

import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.routerMessage
import com.enigmastation.streampack.whiteboard.service.Router
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.support.ApplicationObjectSupport
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
@EnableMethodSecurity
class LLMBot : CommandLineRunner, ApplicationObjectSupport() {
    @Bean fun restTemplate(): RestTemplate = RestTemplate()

    @Bean
    fun getClientHttpRequestFactory(): ClientHttpRequestFactory {
        var clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()
        clientHttpRequestFactory.setConnectTimeout(1000)
        clientHttpRequestFactory.setConnectionRequestTimeout(15000)
        return clientHttpRequestFactory
    }

    //    @Bean
    //    fun userDetailsService(): UserDetailsService {
    //        val users: User.UserBuilder = User.withDefaultPasswordEncoder()
    //        val manager = InMemoryUserDetailsManager()
    //        manager.createUser(users.username("user").password("password").roles("USER").build())
    //        manager.createUser(
    //            users.username("admin").password("password").roles("USER", "ADMIN").build()
    //        )
    //        return manager
    //    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests { authorize(anyRequest, permitAll) }
            httpBasic {}
        }
        return http.build()
    }

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
    runApplication<LLMBot>(*args)
}
