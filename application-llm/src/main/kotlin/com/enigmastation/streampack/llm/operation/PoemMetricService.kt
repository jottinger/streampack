/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.llm.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterService
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Service

@Service
class PoemMetricService(val ollamaChatModel: OllamaChatModel) : RouterService() {
    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.startsWith("poem: ")
    }

    override fun handleMessage(message: RouterMessage) {
        if (canHandle(message)) {
            val poemtext = message.content.removePrefix("poem: ")
            val response =
                ollamaChatModel.call(
                    "is the poem ```$poemtext``` in iambic pentameter? include a sample of the poem, answer succinctly, please, in one line and with less than 300 characters."
                )
            logger.info("response: $response")
            dispatch(message.respondWith(response.compress()))
        }
    }
}
