/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.summarize.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.extensions.toURL
import com.enigmastation.streampack.summarize.model.SummarizeConfiguration
import com.enigmastation.streampack.summarize.service.SummarizeService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.stereotype.Service

@Service
class SummarizeRouterOperation(
    val configuration: SummarizeConfiguration,
    val summarizeService: SummarizeService
) : RouterOperation() {
    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        return try {
            val url = message.content.compress().removePrefix("~summarize ").split(" ")[0].toURL()
            val result = summarizeService.summarizeURL(url)
            val content =
                "Summary for $url: ${result.summary}" +
                    when ((result.categories ?: listOf()).isEmpty()) {
                        true -> ", categorization is uncertain"
                        else ->
                            " Categorizations were: ${result.categories!!.joinToStringWithAnd()}"
                    }
            message.respondWith(content)
        } catch (e: Throwable) {
            logger.info(e.message, e)
            null
        }
    }

    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.startsWith("~summarize ") &&
            configuration.services.contains(message.messageSource)
    }

    override fun description(): String {
        return "Use '~summarize [url]' to get a short summary of the url content, plus an attempt at categorization."
    }

    override fun longDescription(): String {
        return super.longDescription()
    }
}
