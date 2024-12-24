package com.enigmastation.streampack.slack.service

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.App
import com.slack.api.bolt.AppConfig
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.bolt.handler.BoltEventHandler
import com.slack.api.bolt.jakarta_socket_mode.SocketModeApp
import com.slack.api.bolt.response.Response
import com.slack.api.methods.request.conversations.ConversationsListRequest
import com.slack.api.model.event.MessageEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SlackService : BoltEventHandler<MessageEvent> {
    @Autowired
    lateinit var configuration: SlackServiceConfiguration
    lateinit var app: App

    fun join() {
        val appConfig = AppConfig
            .builder()
            .singleTeamBotToken(configuration.botToken)
            .build();
        app = App(appConfig)
        app.event(MessageEvent::class.java, this)
        SocketModeApp(configuration.appToken,app).start()


        println("joined")
//        app.message(Pattern.compile("a*b"), this)
        println("channels")
        val r = app.client.conversationsList(ConversationsListRequest.builder().build())
        println(r.channels.map { it.name })
    }

    override fun apply(
        event: EventsApiPayload<MessageEvent?>?,
        context: EventContext?
    ): Response? {
        println("Got a message event: $event")
        return Response.ok()
    }

}
