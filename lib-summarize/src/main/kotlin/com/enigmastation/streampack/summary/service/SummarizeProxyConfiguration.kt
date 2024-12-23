/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.summary.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.summarize.proxy")
@Component
class SummarizeProxyConfiguration {
    var proxyUrl: String = "http://gptproxy:8084/"
    var enabled: Boolean = true
}
