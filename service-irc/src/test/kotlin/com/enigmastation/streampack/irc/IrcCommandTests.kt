/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.irc

import com.enigmastation.streampack.irc.service.IrcRouterService
import com.enigmastation.streampack.irc.service.commands.JoinCommand
import com.enigmastation.streampack.irc.service.commands.LeaveCommand
import com.enigmastation.streampack.irc.service.commands.SetCommand
import com.enigmastation.streampack.irclog.service.ChannelService
import com.enigmastation.streampack.whiteboard.model.routerMessage
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class IrcCommandTests {
    lateinit var channelService: ChannelService

    lateinit var routerService: IrcRouterService

    lateinit var joinCommand: JoinCommand
    lateinit var leaveCommand: LeaveCommand
    lateinit var setCommand: SetCommand

    @BeforeEach
    fun `clear channels`() {
        routerService = mockk<IrcRouterService>()
        every { routerService.join(any()) } returns Unit
        every { routerService.leave(any()) } returns Unit
        every { routerService.mute(any(), any()) } returns Unit

        channelService = mockk<ChannelService>()
        every { channelService.mute(any(), any(), any(), any()) } returns Unit
        every { channelService.autojoin(any(), any(), any(), any()) } returns Unit
        every { channelService.visible(any(), any(), any(), any()) } returns Unit
        every { channelService.logged(any(), any(), any(), any()) } returns Unit

        joinCommand = JoinCommand(routerService)
        leaveCommand = LeaveCommand(routerService)
        setCommand = SetCommand(routerService, channelService)
    }

    @Test
    fun `join channel`() {
        joinCommand.receive(routerMessage { content = "~join #foo" })
        verify { routerService.join("#foo") }
    }

    @Test
    fun `join not a channel`() {
        joinCommand.receive(routerMessage { content = "~join foo" })
        verify(exactly = 0) { routerService.join(any()) }
    }

    @Test
    fun `join no arguments`() {
        joinCommand.receive(routerMessage { content = "~join" })
        verify(exactly = 0) { routerService.join(any()) }
    }

    @Test
    fun `leave channel`() {
        leaveCommand.receive(routerMessage { content = "~leave #foo" })
        verify { routerService.leave("#foo") }
    }

    @Test
    fun `set mute on`() {
        setCommand.receive(routerMessage { content = "~set mute #foo on" })
        verify { routerService.mute("#foo", true) }
    }

    @Test
    fun `set mute off`() {
        setCommand.receive(routerMessage { content = "~set mute #foo off" })
        verify { routerService.mute("#foo", false) }
    }

    @Test
    fun `set autojoin on`() {
        setCommand.receive(routerMessage { content = "~set autojoin #foo true" })
        verify { routerService.autojoin("#foo", true) }
    }

    @Test
    fun `set autojoin off`() {
        setCommand.receive(routerMessage { content = "~set autojoin #foo false" })
        verify { routerService.autojoin("#foo", false) }
    }

    @Test
    fun `set auto_join on`() {
        setCommand.receive(routerMessage { content = "~set auto_join #foo yes" })
        verify { routerService.autojoin("#foo", true) }
    }

    @Test
    fun `set auto_join off`() {
        setCommand.receive(routerMessage { content = "~set auto_join #foo no" })
        verify { routerService.autojoin("#foo", false) }
    }

    @Test
    fun `set visible on`() {
        setCommand.receive(routerMessage { content = "~set visible #foo on" })
        verify { routerService.visible("#foo", true) }
    }

    @Test
    fun `set visible off`() {
        setCommand.receive(routerMessage { content = "~set visible #foo off" })
        verify { routerService.visible("#foo", false) }
    }

    @Test
    fun `set logged on`() {
        setCommand.receive(routerMessage { content = "~set logged #foo on" })
        verify { routerService.logged("#foo", true) }
    }

    @Test
    fun `set logged off`() {
        setCommand.receive(routerMessage { content = "~set logged  #foo off" })
        verify { routerService.logged("#foo", false) }
    }

    @Test
    fun `invalid set field`() {
        setCommand.receive(routerMessage { content = "~set blash #foo on" })
        verify(exactly = 0) { routerService.logged(any(), any()) }
        verify(exactly = 0) { routerService.mute(any(), any()) }
        verify(exactly = 0) { routerService.autojoin(any(), any()) }
        verify(exactly = 0) { routerService.visible(any(), any()) }
    }

    @Test
    fun `invalid field value`() {
        setCommand.receive(routerMessage { content = "~set mute #foo akjhasdn" })
        verify(exactly = 0) { routerService.logged(any(), any()) }
        verify(exactly = 0) { routerService.mute(any(), any()) }
        verify(exactly = 0) { routerService.autojoin(any(), any()) }
        verify(exactly = 0) { routerService.visible(any(), any()) }
    }

    @Test
    fun `no field value`() {
        setCommand.receive(routerMessage { content = "~set mute #foo" })
        verify(exactly = 0) { routerService.logged(any(), any()) }
        verify(exactly = 0) { routerService.mute(any(), any()) }
        verify(exactly = 0) { routerService.autojoin(any(), any()) }
        verify(exactly = 0) { routerService.visible(any(), any()) }
    }

    @Test
    fun `not a channel`() {
        setCommand.receive(routerMessage { content = "~set mute foo on" })
        verify(exactly = 0) { routerService.logged(any(), any()) }
        verify(exactly = 0) { routerService.mute(any(), any()) }
        verify(exactly = 0) { routerService.autojoin(any(), any()) }
        verify(exactly = 0) { routerService.visible(any(), any()) }
    }
}
