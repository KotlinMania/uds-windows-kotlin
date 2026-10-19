package io.github.kotlinmania.udswindows.stdnet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class CompletionTest {
    @Test
    fun completionDispatchCallsEachImplementationOnce() {
        val stream = UnixStream.fromRawSocket(101L)
        val listener = UnixListener.fromRawSocket(202L)
        var connects = 0
        var accepts = 0
        val streamExtension =
            object : UnixStreamExt by stream {
                override fun connectComplete() {
                    connects++
                    stream.connectComplete()
                }
            }
        val listenerExtension =
            object : UnixListenerExt by listener {
                override fun acceptComplete(socket: UnixStream) {
                    accepts++
                    assertSame(stream, socket)
                    listener.acceptComplete(socket)
                }
            }
        try {
            assertEquals(Unit, connectComplete(streamExtension))
            assertEquals(Unit, acceptComplete(listenerExtension, stream))
            assertEquals(1, connects)
            assertEquals(1, accepts)
        } finally {
            stream.close()
            listener.close()
        }
    }

    @Test
    fun completionDispatchPreservesTheThrownException() {
        val stream = UnixStream.fromRawSocket(103L)
        val failure = WindowsSocketException(10060, "Completion timed out")
        var calls = 0
        val extension =
            object : UnixStreamExt by stream {
                override fun connectComplete() {
                    calls++
                    throw failure
                }
            }
        try {
            val thrown = assertFailsWith<WindowsSocketException> { connectComplete(extension) }
            assertSame(failure, thrown)
            assertEquals(1, calls)
            assertEquals(10060, thrown.rawOsError)
            assertEquals("Completion timed out", thrown.message)
        } finally {
            stream.close()
        }
    }
}
