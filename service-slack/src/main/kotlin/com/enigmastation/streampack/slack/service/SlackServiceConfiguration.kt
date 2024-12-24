package com.enigmastation.streampack.slack.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.slack")
@Component
class SlackServiceConfiguration {
    var botToken: String = ""
    var signingSecret: String = ""
    var appToken:String=""

    override fun toString(): String {
        return "SlackServiceConfiguration[appToken='$appToken',botToken='$botToken', signingSecret='$signingSecret']"
    }
}
