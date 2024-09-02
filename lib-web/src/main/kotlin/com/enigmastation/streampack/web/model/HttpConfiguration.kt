/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.web.model

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.http")
@Component
class HttpConfiguration {
    var cacheDir = "./.cache"
    var userAgent =
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"
    var cacheSize: Long = 100
}
