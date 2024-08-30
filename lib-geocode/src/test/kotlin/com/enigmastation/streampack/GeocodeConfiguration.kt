/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate

@SpringBootApplication
class GeocodeConfiguration {
    @Bean fun restTemplate() = RestTemplate()
}
