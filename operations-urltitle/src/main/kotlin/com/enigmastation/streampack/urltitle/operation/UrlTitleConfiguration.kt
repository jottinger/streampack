/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.operation

import com.enigmastation.streampack.whiteboard.model.MessageSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.urltitle")
@Component
class UrlTitleConfiguration {
    // normally applies ONLY to message sources that don't auto-expand urls as part of their clients
    var services = arrayOf(MessageSource.IRC)
    var defaultIgnoredHosts =
        arrayOf(
            "/twitter.com",
            "/x.com",
            "/bpa.st",
            "/dpaste.com",
            "/pastebin.com",
            "/pastebin.org"
        )
}
