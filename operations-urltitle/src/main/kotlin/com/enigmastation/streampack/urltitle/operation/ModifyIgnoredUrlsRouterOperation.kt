/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.urltitle.operation

import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.urltitle.service.UrlTitleService
import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import com.enigmastation.streampack.whiteboard.model.routerMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ModifyIgnoredUrlsRouterOperation : RouterOperation() {
    @Autowired lateinit var urlTitleService: UrlTitleService

    override fun canHandle(message: RouterMessage): Boolean {
        return message.compress().lowercase().startsWith("~url ignore ")
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val commands = message.compress().lowercase().removePrefix("~url ignore ").split(' ')
        return try {
            when (commands[0]) {
                "list" ->
                    message.respondWith(
                        content =
                            "Banned hosts include: " +
                                urlTitleService
                                    .findBannedHosts()
                                    .shuffled()
                                    .take(7)
                                    .map { it.removePrefix("/") }
                                    .joinToStringWithAnd()
                    )
                "add" -> {
                    urlTitleService.addIgnoredHost(commands[1])
                    routerMessage { scope = MessageScope.TERMINAL }
                }
                "delete" -> {
                    urlTitleService.deleteIgnoredHost("/${commands[1].removePrefix("/")}")
                    routerMessage { scope = MessageScope.TERMINAL }
                }
                else -> null
            }
        } catch (e: Throwable) {
            logger.info("{}", e.message)
            routerMessage { scope = MessageScope.TERMINAL }
        }
    }

    override fun description(): String {
        return "~url ignore [list|add|delete] [hostname]"
    }
}
