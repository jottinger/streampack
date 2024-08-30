/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.model

import java.time.Duration

/**
 * A RouterOperation is component that can:
 * 1. Evaluate whether a message *might* be appropriate for handling
 * 2. Handle a message, returning a *transformed* message if it is handled
 *
 * It can indicate that a message is terminal (not for future processing) by setting the message's
 * scope to `MessageScope.TERMINAL` - this means that processing ends and the message is no longer
 * handed to *any* subsequent service, transformation, or service.
 *
 * `TERMINAL` is suitable when a message is consumed (i.e., a delete service that shouldn't
 * propagate if it matches; see how the example RSS service handles adds or deletes. If they're
 * successful, then the processing of that message is finished, and thus they return TERMINAL for
 * the message scope.)
 *
 * A RouterOperation also has a `priority`. The default is 10, but operations can adjust this as
 * necessary; operations are ordered in *ascending* order by priority (thus, a priority of 1 is
 * "before" a priority of 10) but operations of equivalent priority are not ordered stably (i.e.,
 * there's no guarantee that one service of priority 10 will always execute before another service
 * of priority 10.)
 */
abstract class RouterOperation(
    name: String? = null,
    var priority: Int = 10,
    timeout: Duration? = null
) : Comparable<RouterOperation>, NamedService(name, timeout) {
    final override fun compareTo(other: RouterOperation): Int {
        return priority.compareTo(other.priority)
    }

    final override fun receive(message: RouterMessage): RouterMessage? {
        return handleMessage(message)?.copy(operation = name)
    }

    /**
     * By default, every service consumes every message. if they have a response, the operations are
     * terminal: no further operations are tested. However, transformations will apply, and the
     * results will be dispatched to all appropriate services *unless* the message scope is set to
     * TERMINAL.
     */
    open fun handleMessage(message: RouterMessage): RouterMessage? = null
}
