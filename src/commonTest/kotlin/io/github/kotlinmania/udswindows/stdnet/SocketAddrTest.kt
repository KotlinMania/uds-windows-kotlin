// port-lint: source stdnet/mod.rs
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
        val error = assertFailsWith<IllegalArgumentException> {
            fromPath("bad\u0000path")
        }

        assertEquals("paths may not contain interior null bytes", error.message)
    }

    @Test
    fun sunPathLengthMustLeaveRoomForNullTerminator() {
        val error = assertFailsWith<IllegalArgumentException> {
            fromPath("a".repeat(108))
        }

        assertEquals("path must be shorter than SUN_LEN", error.message)
    }
}
