// port-lint: tests stdnet/mod.rs
package io.github.kotlinmania.udswindows.stdnet

import kotlin.test.Test
import kotlin.test.assertEquals

class ModTest {
    @Test
    fun testSocketError() {
        val error = SocketError(10054, "Connection reset")
        assertEquals(10054, error.rawOsError)
        assertEquals("Connection reset", error.message)
    }

    @Test
    fun testWindowsSocketException() {
        val ex = WindowsSocketException(10060, "Timeout")
        assertEquals(10060, ex.rawOsError)
        assertEquals("Timeout", ex.message)
    }
}
