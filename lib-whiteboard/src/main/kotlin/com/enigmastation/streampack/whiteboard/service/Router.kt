/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.whiteboard.service

import com.enigmastation.streampack.whiteboard.model.MessageScope
import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.NamedService
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.RouterOperation
import com.enigmastation.streampack.whiteboard.model.RouterService
import com.enigmastation.streampack.whiteboard.model.RouterTransformer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

@Service
class Router(
    val services: List<RouterService>,
    val routerOperations: List<RouterOperation>,
    val routerTransformers: List<RouterTransformer>
): InitializingBean {
    val logger = LoggerFactory.getLogger(this::class.java)

    val executorService = Executors.newVirtualThreadPerTaskExecutor()
    val operationTimeouts = mutableMapOf<String, Int>()

    /**
     * The flow here is "interesting."
     *
     * A RouterMessage has three very important attributes: the content ("what's the message"), the
     * type ("is this an internal message or an external one?" - i.e., system versus service
     * commands), and the service (the source of the RouterMessage, which might be a service name,
     * for example.)
     *
     * When a message comes in, its type gets checked.
     *
     * If it's INTERNAL, it's dispatched to each registered service as a command. The routing ends
     * at this point.
     *
     * If it's not INTERNAL, it gets sent to each registered RouterOperation, in order of priority
     * (ascending, so a priority of 9 is processed before a priority of 10). A RouterOperation that
     * returns a RouterMessage is terminal; no subsequent operations will be invoked. Thus, the
     * first RouterOperation that responds with a RouterMessage is considered as having "handled"
     * the input. A RouterOperation sets the "service" of the RouterMessage to its own name, which
     * is *important*.
     *
     * If a RouterOperation handles the message, it's then passed through *every* RouterTransformer.
     * RouterTransformers are not allowed to terminate processing at present; they *must* return a
     * RouterMessage.
     * > Eventually, we may work out a way for a RouterTransformer to terminate processing: imagine
     * > a RouterTransformer that culls spam, for example.
     *
     * If no RouterOperation handles the message, the original input RouterMessage is used; this is
     * *not* passed through the Transformation mechanism.
     *
     * After (potential) handling through service and transformation, the resulting message is
     * dispatched to every service that can handle the message, *unless* the service value matches
     * the service name (which should *only* happen if no service handled the message, as
     * RouterOperations naturally assign their name to the service value). Thus, a service will not
     * (or should not!) get the same message it issued, ever; it will only receive messages that
     * have been mutated and which it indicates are appropriate for the service in question.
     */
    fun dispatch(message: RouterMessage) {
        executorService.submit {
            logger.debug("dispatching message {}", message)
            when (message.messageSource) {
                MessageSource.INTERNAL ->
                    services.forEach { service -> service.handleCommand(message) }
                else -> {
                    val runner = Executors.newVirtualThreadPerTaskExecutor()
                    var captured: RouterMessage =
                        when (message.process) {
                            true -> {
                                // for each transformer, we see if it is interested in the message,
                                // and if so, we look for the responses by priority
                                var response: RouterMessage? = null
                                for (operation in routerOperations.sorted()) {
                                    response = evaluateAndDispatch(operation, message, runner)
                                    if (response != null) {
                                        break
                                    }
                                }
                                logger.debug("raw response from service: {}", response)
                                when (response) {
                                    null -> message
                                    else ->
                                        when (response.scope) {
                                            MessageScope.TERMINAL -> response
                                            else -> {
                                                var original = response
                                                for (transformer in routerTransformers.sorted()) {
                                                    var internalResponse =
                                                        evaluateAndDispatch(
                                                            transformer,
                                                            response!!,
                                                            runner
                                                        )
                                                    response = internalResponse ?: original
                                                    logger.debug(
                                                        "{} {} {}",
                                                        transformer.name,
                                                        response,
                                                        internalResponse
                                                    )
                                                }
                                                response
                                            }
                                        }
                                }
                            }
                            else -> message
                        }
                    // now that we have the responses, IF we have a "first" response, we send it
                    // back to the services that can handle the message type... and that don't match
                    // the service value
                    if (captured.scope != MessageScope.TERMINAL) {
                        services
                            .filterNot { service -> captured.operation == service.name }
                            .filter { service -> service.canHandle(captured) }
                            // we don't care about timeouts for router services.
                            // They just get the messages, fire and forget.
                            .forEach { service -> service.receive(captured) }
                    }
                }
            }
        }
    }

    private fun evaluateAndDispatch(
        operation: NamedService,
        message: RouterMessage,
        service: ExecutorService
    ): RouterMessage? {
        logger.debug("Testing suitability for {}", operation.name)
        return if (operation.canHandle(message)) {
            if ((operationTimeouts[operation.name] ?: 0) > 5) {
                logger.info(
                    "Operation {} has {} timeouts: disabled",
                    operation.name,
                    operationTimeouts[operation.name]!!
                )
                null
            } else {
                logger.debug("Dispatching to {}", operation.name)
                dispatchWithTimeout(operation, message, service)
            }
        } else {
            null
        }
    }

    private fun dispatchWithTimeout(
        operation: NamedService,
        message: RouterMessage,
        runner: ExecutorService
    ): RouterMessage? {
        val future =
            CompletableFuture.supplyAsync<RouterMessage?>({ operation.receive(message) }, runner)
        var response =
            try {
                future.get(operation.timeout.toMillis(), TimeUnit.MILLISECONDS)
            } catch (t: TimeoutException) {
                operationTimeouts.merge(operation.name, 1) { prev: Int, new: Int -> prev + new }
                logger.info("Call timed out: {}", operation.name)
                logger.info("failures: {}", operationTimeouts)
                null
            } catch (t: Throwable) {
                logger.info("exception: {}", t.message, t)
                null
            } finally {
                future.cancel(true)
            }
        return response
    }

    override fun afterPropertiesSet() {
        services
            // TODO under what conditions would we not want a service to be dispatched to? Any at
            // all?
            .filter { true }
            .forEach { service ->
                logger.debug("Adding router to service {}", service.name)
                service.addRouter(this)
            }
        routerOperations.forEach { logger.info("Registered service: {}", it.name) }
        routerTransformers.forEach { logger.info("Registered transformer: {}", it.name) }
    }

//    init {
//        // for each service, we need to make sure it knows about this router
//        services
//            // TODO under what conditions would we not want a service to be dispatched to? Any at
//            // all?
//            .filter { true }
//            .forEach { service ->
//                logger.debug("Adding router to service {}", service.name)
//                service.addRouter(this)
//            }
//        routerOperations.forEach { logger.info("Registered service: {}", it.name) }
//        routerTransformers.forEach { logger.info("Registered transformer: {}", it.name) }
//    }
}
