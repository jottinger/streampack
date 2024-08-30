/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.llm.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterService
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.stereotype.Service

@Service
class PoemCreationService(val ollamaChatModel: OllamaChatModel) : RouterService() {
    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.startsWith("~poem ")
    }

    override fun handleMessage(message: RouterMessage) {
        if (!canHandle(message)) {
            return
        }
        val query = message.content.removePrefix("~poem ").compress()
        val response =
            ollamaChatModel.call(
                "create a short poem in less than 300 characters, with $query as a topic"
            )
        println(response.compress().length)
        dispatch(message.respondWith("poem: ${response.compress().replace("\n", "/")}"))
    }
}
