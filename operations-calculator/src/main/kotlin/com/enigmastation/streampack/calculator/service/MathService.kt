/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.calculator.service

import org.springframework.stereotype.Service
import parser.MathExpression

@Service
class MathService {
    fun evaluate(expression: String): Any? {
        return MathExpression(expression).solve()
    }
}
