/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.karma.operation

import com.enigmastation.streampack.karma.entity.KarmaEntry
import com.enigmastation.streampack.karma.repository.KarmaEntryRepository
import com.enigmastation.streampack.karma.service.KarmaEntryService
import com.enigmastation.streampack.whiteboard.model.MessageSource
import com.enigmastation.streampack.whiteboard.model.RouterMessage
import com.enigmastation.streampack.whiteboard.model.routerMessage
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class KarmaOperationTests {
    data class KarmaTestCommand(val origin: String, val message: String, val result: String)

    @Autowired lateinit var setKarmaOperation: SetKarmaOperation

    @Autowired lateinit var getKarmaOperation: GetKarmaOperation

    @Autowired lateinit var karmaEntryRepository: KarmaEntryRepository

    @Autowired lateinit var karmaEntryService: KarmaEntryService

    @BeforeTest
    fun `clear all`() {
        karmaEntryRepository.deleteAll()
        setKarmaOperation.commentsEnabled = true
    }

    fun verify(message: RouterMessage?, substring: String) {
        val content =
            message?.content
                ?: if (substring.isNotEmpty()) {
                    throw IllegalArgumentException(
                        "Message content is invalid: $message, for $substring"
                    )
                } else return
        if (!content.lowercase().contains(substring.lowercase())) {
            throw IllegalArgumentException(
                "\"${content.lowercase()}\" did not contain \"${substring.lowercase()}\""
            )
        }
    }

    @Test
    fun `test karma path`() {
        val commands =
            listOf(
                KarmaTestCommand("bar", "~karma bar", "bar, you have no karma data."),
                KarmaTestCommand("bar", "~foo++", "foo now has karma of 1."),
                KarmaTestCommand("bar", "~foo++", "foo now has karma of 2."),
                KarmaTestCommand("bar", "~foo: -- whoa they suck", "foo now has karma of 1."),
                KarmaTestCommand("bar", "~foo: ++ no they don't", "foo now has karma of 2."),
                KarmaTestCommand("bar", "foo--", "foo now has karma of 1."),
                KarmaTestCommand("bar", "foo++", "foo now has karma of 2."),
                KarmaTestCommand("bar", "~foo:-- yes they do", "foo now has karma of 1."),
                KarmaTestCommand("bar", "~karma foo", "foo has karma of 1."),
                KarmaTestCommand("bar", "~foo:-- why did I do this", "foo has neutral karma."),
                KarmaTestCommand(
                    "bar",
                    "~bar:++ I'm the best",
                    "You can't increment your own karma! Your karma is now -1."
                ),
                KarmaTestCommand("bar", "~karma bar", "bar, you have karma of -1."),
                KarmaTestCommand("bar", "~karma foo", "foo has neutral karma."),
                KarmaTestCommand("foo", "~karma foo", "foo, you have neutral karma."),
                KarmaTestCommand("bar", "~bar-- I'm the worst", "Your karma is now -2."),
                KarmaTestCommand("bar", "foo ++", "foo now has karma of 1."),
                KarmaTestCommand("bar", "C++", "c now has karma of 1."),
                KarmaTestCommand("bar", "C++++", "c++ now has karma of 1."),
                KarmaTestCommand("bar", "C++--", "c++ has neutral karma."),
                KarmaTestCommand("bar", "C++++++", "c++++ now has karma of 1."),
                KarmaTestCommand("bar", "C++ : ++", "c++ now has karma of 1."),
                KarmaTestCommand("bar", "++", ""),
                KarmaTestCommand("bar", "I hate this --> ++", "i hate this -> now has karma of 1."),
                KarmaTestCommand("bar", "I hate this --> --", "i hate this -> has neutral karma."),
                KarmaTestCommand("bar", "I hate this <-- ++", "i hate this <- now has karma of 1."),
                KarmaTestCommand("bar", "I hate this <-- --", "i hate this <- has neutral karma.")
            )
        commands.forEach { command ->
            with(command) {
                val message = routerMessage {
                    content = message
                    source = origin
                    messageSource = MessageSource.IRC
                }
                //                println("testing: $message")
                verify(
                    getKarmaOperation.handleMessage(message)
                        ?: setKarmaOperation.handleMessage(message),
                    result
                )
            }
        }
    }

    @Test
    fun `test aged karma`() {
        // we want to create forty karma entries, covering twenty weeks
        var date = OffsetDateTime.now()
        for (i in 0..52) {
            karmaEntryRepository.save(
                KarmaEntry(
                    selector = "baz",
                    increment = 1,
                    createTimestamp = date.minus((40 - i).toLong(), ChronoUnit.WEEKS)
                )
            )
        }
        // 52 entries, should be 44
        assertEquals(44, karmaEntryService.getKarma("baz"))
    }
}
