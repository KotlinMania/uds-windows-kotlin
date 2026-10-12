// port-lint: tests stdnet/net.rs
package io.github.kotlinmania.udswindows.stdnet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NetTest {
    @Test
    fun basic() {
        val socketPath = "/tmp/test_sock_basic_${randomSuffix()}"
        val msg1 = "hello".encodeToByteArray()
        val msg2 = "world!".encodeToByteArray()

        val listener = UnixListener.bind(socketPath)
        val stream = UnixStream.connect(socketPath)

        val accepted = listener.accept()
        val serverStream = accepted.stream

        stream.write(msg1)
        val buf = ByteArray(5)
        val n = serverStream.read(buf)
        assertEquals(5, n)
        assertTrue(msg1.contentEquals(buf))

        serverStream.write(msg2)
        val readBytes = ByteArray(6)
        val n2 = stream.read(readBytes)
        assertEquals(6, n2)
        assertTrue(msg2.contentEquals(readBytes))

        listener.close()
        stream.close()
        serverStream.close()
    }

    @Test
    fun tryClone() {
        val socketPath = "/tmp/test_sock_clone_${randomSuffix()}"
        val msg1 = "hello".encodeToByteArray()
        val msg2 = "world".encodeToByteArray()

        val listener = UnixListener.bind(socketPath)
        val stream = UnixStream.connect(socketPath)
        val accepted = listener.accept()
        val serverStream = accepted.stream

        val stream2 = stream.tryClone()

        serverStream.write(msg1)
        val buf1 = ByteArray(5)
        val n1 = stream.read(buf1)
        assertEquals(5, n1)
        assertTrue(msg1.contentEquals(buf1))

        serverStream.write(msg2)
        val buf2 = ByteArray(5)
        val n2 = stream2.read(buf2)
        assertEquals(5, n2)
        assertTrue(msg2.contentEquals(buf2))

        listener.close()
        stream.close()
        stream2.close()
        serverStream.close()
    }

    @Test
    fun iter() {
        val socketPath = "/tmp/test_sock_iter_${randomSuffix()}"
        val listener = UnixListener.bind(socketPath)

        val stream1 = UnixStream.connect(socketPath)
        val stream2 = UnixStream.connect(socketPath)

        var count = 0
        for (conn in listener.incoming().take(2)) {
            assertNotNull(conn.stream)
            count++
        }
        assertEquals(2, count)

        listener.close()
        stream1.close()
        stream2.close()
    }

    @Test
    fun longPath() {
        val longPath =
            "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfa" +
                "sasdfasdfasdasdfasdfasdfadfasdfasdfasdfasdfasdf"
        assertFailsWith<IllegalArgumentException> {
            UnixStream.connect(longPath)
        }
        assertFailsWith<IllegalArgumentException> {
            UnixListener.bind(longPath)
        }
    }

    @Test
    fun abstractNamespaceNotAllowed() {
        assertFailsWith<IllegalArgumentException> {
            UnixStream.connect("\u0000asdf")
        }
        assertFailsWith<IllegalArgumentException> {
            UnixListener.bind("\u0000asdf")
        }
    }

    @Test
    fun rawSocketConversions() {
        val raw = 42L
        val socket = Socket.fromRawSocket(raw)
        assertEquals(raw, socket.asRawSocket())
        assertEquals(raw, socket.intoRawSocket())
        assertTrue(socket.isClosed())

        val stream = UnixStream.fromRawSocket(101L)
        assertEquals(101L, stream.asRawSocket())
        stream.flush()
        stream.connectComplete()
        assertEquals(101L, stream.intoRawSocket())

        val listener = UnixListener.fromRawSocket(202L)
        assertEquals(202L, listener.asRawSocket())
        listener.acceptComplete(stream)
        assertEquals(202L, listener.intoRawSocket())
    }

    @Test
    fun incomingSizeHintAndFormatting() {
        val socketPath = "/tmp/test_sock_incoming_${randomSuffix()}"
        val listener = UnixListener.bind(socketPath)
        val incoming = listener.incoming()
        val (lower, upper) = incoming.sizeHint()
        assertEquals(Long.MAX_VALUE, lower)
        assertEquals(null, upper)
        assertTrue(incoming.toString().contains("Incoming"))
        listener.close()
    }

    @Test
    fun acceptAddrsExtensions() {
        val buf = AcceptAddrsBuf.new()
        val socketPath = "/tmp/test_sock_addrs_${randomSuffix()}"
        val listener = UnixListener.bind(socketPath)
        val parsed = buf.parse(listener)
        assertNotNull(parsed.local())
        assertNotNull(parsed.remote())
        assertTrue(buf.toString().contains("AcceptAddrsBuf"))
        assertTrue(parsed.toString().contains("AcceptAddrs"))
        assertEquals(buf, AcceptAddrsBuf.new())
        listener.close()
    }

    private fun randomSuffix(): String = (1..100000).random().toString()
}
