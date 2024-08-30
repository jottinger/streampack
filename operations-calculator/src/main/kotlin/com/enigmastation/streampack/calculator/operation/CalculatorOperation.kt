/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.calculator.operation

import com.enigmastation.streampack.calculator.service.MathService
import com.enigmastation.streampack.extensions.compress
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.stereotype.Service

@Service
class CalculatorOperation(val service: MathService) : RouterOperation(priority = 9) {
    override fun canHandle(message: RouterMessage): Boolean {
        return message.content.startsWith("~calc ")
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        val expression = message.content.removePrefix("~calc ")
        return try {
            val result = service.evaluate(expression).toString().toBigDecimalOrNull()
            if (result != null) {
                message.respondWith("The result of ${expression.compress()} is: $result")
            } else {
                null
            }
        } catch (e: Throwable) {
            logger.error(e.message)
            null
        }
    }
}
