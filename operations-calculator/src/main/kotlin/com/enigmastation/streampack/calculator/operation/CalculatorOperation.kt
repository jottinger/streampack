/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.calculator.operation

import com.enigmastation.streampack.calculator.service.MathService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import org.springframework.stereotype.Service

@Service
class CalculatorOperation(val service: MathService) : RouterOperation(priority = 9) {

    override fun canHandle(message: RouterMessage): Boolean {
        val parser = CalcOperationGrammar.parser()
        return parser.run(message.content).matched
    }

    override fun handleMessage(message: RouterMessage): RouterMessage? {
        val parser = CalcOperationGrammar.parser()
        return parser.run(message.content).stackTop?.let { expression ->
            try {
                val result = service.evaluate(expression).toString().toBigDecimalOrNull()
                if (result != null) {
                    message.respondWith("The result of $expression is: $result")
                } else {
                    null
                }
            } catch (e: Throwable) {
                logger.error(e.message)
                null
            }
        }
    }

    override fun description(): String {
        return "Evaluates any mathematical operation provided after \"~calc\" - try \"calc 4*sin(3)^2\""
    }

    override fun longDescription(): String {
        return super.longDescription()
    }
}
