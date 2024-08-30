/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.model

/**
 * A RouterTransformer is, well, a transformation mechanism. It takes output from an service or
 * prior transformation, potentially mutates it, and writes the result back into the stream.
 *
 * It should not take a long time. Ever. These things should be quick.
 *
 * It should ALWAYS return a transformed object; a `null` being returned means "use the input as it
 * was."
 */
abstract class RouterTransformer(name: String? = null, var priority: Int = 10) :
    NamedService(name), Comparable<RouterTransformer> {
    final override fun compareTo(other: RouterTransformer): Int {
        return priority.compareTo(other.priority)
    }

    override fun canHandle(message: RouterMessage): Boolean {
        return true
    }

    final override fun receive(message: RouterMessage): RouterMessage {
        return if (canHandle(message)) {
            handleMessage(message) ?: message
        } else {
            message
        }
    }

    open fun handleMessage(message: RouterMessage): RouterMessage? = null
}
