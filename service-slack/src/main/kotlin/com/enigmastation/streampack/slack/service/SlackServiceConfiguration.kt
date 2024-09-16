package com.enigmastation.streampack.slack.service

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties("streampack.slack")
@Component
class SlackServiceConfiguration {
    var botToken: String = ""
    var signingSecret: String = ""

    override fun toString(): String {
        return "SlackServiceConfiguration[botToken='$botToken', signingSecret='$signingSecret']"
    }
}
