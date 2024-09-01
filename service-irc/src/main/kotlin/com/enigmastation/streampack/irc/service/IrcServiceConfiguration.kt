/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irc.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.irc")
@Component
class IrcServiceConfiguration {
    var nick: String = "streampack"
    var realname: String = "streampack"
    var host: String = "irc.libera.chat"
    var port: Int = 6667
    var sasl: Boolean = true
    var saslAccount: String? = null
    var saslPassword: String? = null
    var channels: String = "#kitteh"
}
