package io.github.kotlinmania.udswindows.stdnet

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SocketAddrTest {
    @Test
    fun pathnameAddressRoundTrips() {
        val addr = fromPath("/tmp/sock")

        assertFalse(addr.isUnnamed())
        assertEquals("/tmp/sock", addr.asPathname())
        assertEquals("\"/tmp/sock\" (pathname)", addr.toString())
    }

    @Test
    fun emptyPathIsUnnamed() {
        val addr = fromPath("")

        assertTrue(addr.isUnnamed())
        assertNull(addr.asPathname())
        assertEquals("(unnamed)", addr.toString())
    }

    @Test
    fun sockaddrLengthIncludesTrailingNullForPathnames() {
        val (_, len) = sockaddrUn("/tmp/sock")

        assertEquals(SUN_PATH_OFFSET + "/tmp/sock".encodeToByteArray().size + 1, len)
    }

    @Test
    fun interiorNullBytesAreRejected() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                fromPath("bad\u0000path")
            }

        assertEquals("paths may not contain interior null bytes", error.message)
    }

    @Test
    fun sunPathLengthMustLeaveRoomForNullTerminator() {
        val error =
            assertFailsWith<IllegalArgumentException> {
                fromPath("a".repeat(108))
            }

        assertEquals("path must be shorter than SUN_LEN", error.message)
    }

    @Test
    fun isMinusOneDetectsSocketError() {
        assertTrue((-1).toByte().isMinusOne())
        assertTrue((-1).toShort().isMinusOne())
        assertTrue((-1).isMinusOne())
        assertTrue((-1L).isMinusOne())

        assertFalse(0.toByte().isMinusOne())
        assertFalse(0.isMinusOne())
        assertFalse(0xFFFFFFFFL.isMinusOne())
    }

    @Test
    fun cvtPassesThroughOnSuccess() {
        assertEquals(42, cvt(42))
        assertEquals(0, cvt(0))
        assertEquals(0xFFFFFFFFL, cvt(0xFFFFFFFFL))
    }

    @Test
    fun cvtThrowsOnSocketError() {
        val thrown = assertFailsWith<WindowsSocketException> { cvt(-1) }
        assertEquals(-1, thrown.rawOsError)
        assertEquals("Windows socket error: -1", thrown.message)
    }

    @Test
    fun conversionSentinelsKeepTheirErrorCodes() {
        val failures =
            listOf(
                assertFailsWith<WindowsSocketException> { cvt((-1).toByte()) },
                assertFailsWith<WindowsSocketException> { cvt((-1).toShort()) },
                assertFailsWith<WindowsSocketException> { cvt(-1L) },
            )
        failures.forEach { failure ->
            assertEquals(-1, failure.rawOsError)
            assertEquals("Windows socket error: -1", failure.message)
        }
        val zero = assertFailsWith<WindowsSocketException> { cvtZ(0) }
        assertEquals(0, zero.rawOsError)
        assertEquals("Windows socket error: 0", zero.message)
        assertEquals(1, cvtZ(1))
    }

    @Test
    fun addressInitializationPropagatesSocketFailureOnce() {
        var calls = 0
        val failure =
            assertFailsWith<WindowsSocketException> {
                SocketAddr.new { _, _ ->
                    calls++
                    -1
                }
            }
        assertEquals(1, calls)
        assertEquals(-1, failure.rawOsError)
        assertEquals("Windows socket error: -1", failure.message)
    }

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
