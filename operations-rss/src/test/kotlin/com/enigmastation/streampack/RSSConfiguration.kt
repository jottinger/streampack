/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack

import com.enigmastation.streampack.web.service.JsoupService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class RSSConfiguration {
    @Bean fun restTemplate(): RestTemplate = RestTemplate()

    @Bean fun jsoupService() = JsoupService()
}
