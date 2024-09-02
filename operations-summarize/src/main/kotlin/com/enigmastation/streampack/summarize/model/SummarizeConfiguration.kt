/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.summarize.model

import com.enigmastation.streampack.whiteboard.model.MessageSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.summarize")
@Component
class SummarizeConfiguration {
    var services = arrayOf(MessageSource.IRC, MessageSource.DISCORD, MessageSource.SLACK)
}
