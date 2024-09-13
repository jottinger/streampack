/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.specs.operation

import com.enigmastation.streampack.specs.grammar.SpecsGrammar
import com.enigmastation.streampack.specs.model.SpecsRequestType
import com.enigmastation.streampack.specs.service.JEPService
import com.enigmastation.streampack.specs.service.JSRService
import com.enigmastation.streampack.specs.service.RFCTitleService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SpecsRouterOperation : RouterOperation() {
    @Autowired lateinit var getTitleService: RFCTitleService

    @Autowired lateinit var jsrService: JSRService

    @Autowired lateinit var jepService: JEPService

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        if (!canHandle(message)) {
            return null
        }
        val result = SpecsGrammar.parser().run(message.content).stackTop!!
        val identifier = result.identifier
        val type = result.type
        // rfc 0 doesn't exist. We know this. Punt out immediately, save the HTTP request.
        val service =
            when (type) {
                SpecsRequestType.JSR -> jsrService
                SpecsRequestType.JEP -> jepService
                SpecsRequestType.RFC -> getTitleService
            }
        return try {
            val (url, title) = service.getTitle(identifier)
            return if (title.isNotEmpty()) {
                message.respondWith("$title ($url)")
            } else {
                null
            }
        } catch (_: Throwable) {
            null
        }
    }

    override fun canHandle(message: RouterMessage): Boolean {
        return SpecsGrammar.parser().run(message.content).stackTop != null
    }
}
