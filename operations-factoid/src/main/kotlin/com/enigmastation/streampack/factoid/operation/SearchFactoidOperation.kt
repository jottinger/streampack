/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.factoid.operation

import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.extensions.joinToStringWithAnd
import com.enigmastation.streampack.factoid.service.FactoidService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.stereotype.Service

@Service
class SearchFactoidOperation(val factoidService: FactoidService) : RouterOperation() {
    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.compress().startsWith("~search ")
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val term = message.content.compress().removePrefix("~search ")
        val factoids = factoidService.searchFactoidsForTerm(term)
        return if (factoids.isEmpty()) {
            message.respondWith("No factoids were found searching for '$term'")
        } else {
            var response = factoids.map { "~$it" }.joinToStringWithAnd()
            message.respondWith(
                "Search for '$term' matched with the following: ${
                    if (response.length > 200) {
                        factoids.take(10).joinToStringWithAnd()
                    } else {
                        response
                    }
                }"
            )
        }
    }
}
