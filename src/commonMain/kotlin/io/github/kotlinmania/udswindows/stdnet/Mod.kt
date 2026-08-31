// port-lint: source uds_windows/src/stdnet/mod.rs
package io.github.kotlinmania.udswindows.stdnet

internal const val AF_UNIX: Int = 1
private const val SUN_PATH_LENGTH: Int = 108
internal const val SUN_PATH_OFFSET: Int = 2

internal class SockaddrUn(
    var sunFamily: Int = AF_UNIX,
    sunPath: ByteArray = ByteArray(SUN_PATH_LENGTH),
) {
    val sunPath: ByteArray = sunPath.copyOf(SUN_PATH_LENGTH)

    override fun equals(other: Any?): Boolean {
        return other is SockaddrUn &&
            sunFamily == other.sunFamily &&
            sunPath.contentEquals(other.sunPath)
    }

    override fun hashCode(): Int {
        return 31 * sunFamily + sunPath.contentHashCode()
    }

    override fun toString(): String {
        val path = decodeSunPath()
        return "sockaddr_un(sun_family=$sunFamily, sun_path=$path)"
    }

    private fun decodeSunPath(): String {
        val end = sunPath.indexOf(0.toByte()).takeIf { it >= 0 } ?: sunPath.size
        return sunPath.copyOfRange(0, end).decodeToString()
    }
}

internal fun sunPathOffset(addr: SockaddrUn): Int {
    return SUN_PATH_OFFSET
}

internal fun sockaddrUn(path: String): Pair<SockaddrUn, Int> {
    if (path.contains('\u0000')) {
        throw IllegalArgumentException("paths may not contain interior null bytes")
    }

    val bytes = path.encodeToByteArray()
    if (bytes.size >= SUN_PATH_LENGTH) {
        throw IllegalArgumentException("path must be shorter than SUN_LEN")
    }

    val addr = SockaddrUn()
    addr.sunFamily = AF_UNIX

    for (i in bytes.indices) {
        addr.sunPath[i] = bytes[i]
    }

    var len = sunPathOffset(addr) + bytes.size
    val firstByte = bytes.firstOrNull()
    if (firstByte != null && firstByte != 0.toByte()) {
        len += 1
    }

    return Pair(addr, len)
}

public data class SocketError(
    val rawOsError: Int,
    val message: String = "Windows socket error: $rawOsError",
)

public class WindowsSocketException(
    public val rawOsError: Int,
    message: String = "Windows socket error: $rawOsError",
) : RuntimeException(message)

internal fun lastError(code: Int = -1): WindowsSocketException {
    return WindowsSocketException(code, "Windows socket error: $code")
}

internal interface IsMinusOne {
    fun isMinusOne(): Boolean
}

internal fun Byte.isMinusOne(): Boolean {
    return this == (-1).toByte()
}

internal fun Short.isMinusOne(): Boolean {
    return this == (-1).toShort()
}

internal fun Int.isMinusOne(): Boolean {
    return this == -1
}

internal fun Long.isMinusOne(): Boolean {
    return this == -1L
}

internal fun cvt(t: Byte): Byte {
    if (t.isMinusOne()) {
        throw lastError(t.toInt())
    } else {
        return t
    }
}

internal fun cvt(t: Short): Short {
    if (t.isMinusOne()) {
        throw lastError(t.toInt())
    } else {
        return t
    }
}

internal fun cvt(t: Int): Int {
    if (t.isMinusOne()) {
        throw lastError(t)
    } else {
        return t
    }
}

internal fun cvt(t: Long): Long {
    if (t.isMinusOne()) {
        throw lastError(t.toInt())
    } else {
        return t
    }
}

internal sealed class AddressKind {
    data object Unnamed : AddressKind()

    data class Pathname(
        val path: String,
    ) : AddressKind()

    class Abstract(
        name: ByteArray,
    ) : AddressKind() {
        val name: ByteArray = name.copyOf()

        override fun equals(other: Any?): Boolean {
            return other is Abstract && name.contentEquals(other.name)
        }

        override fun hashCode(): Int {
            return name.contentHashCode()
        }
    }
}

public class SocketAddr internal constructor(
    internal val addr: SockaddrUn,
    internal val len: Int,
) {
    public fun isUnnamed(): Boolean {
        return address() is AddressKind.Unnamed
    }

    public fun asPathname(): String? {
        val kind = address()
        return if (kind is AddressKind.Pathname) {
            kind.path
        } else {
            null
        }
    }

    internal fun address(): AddressKind {
        val pathLen = len - sunPathOffset(addr)
        val path = addr.sunPath

        if (pathLen == 0) {
            return AddressKind.Unnamed
        } else if (addr.sunPath.isNotEmpty() && addr.sunPath[0] == 0.toByte()) {
            return AddressKind.Abstract(path.copyOfRange(1, pathLen))
        } else {
            val nul = path.indexOf(0.toByte())
            val end = if (nul in 0 until pathLen) nul else pathLen.coerceIn(0, path.size)
            val pathname = path.copyOfRange(0, end).decodeToString()
            return AddressKind.Pathname(pathname)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SocketAddr) return false
        if (len != other.len) return false
        if (addr.sunFamily != other.addr.sunFamily) return false
        return addr.sunPath.contentEquals(other.addr.sunPath)
    }

    override fun hashCode(): Int {
        return 31 * len + addr.hashCode()
    }

    override fun toString(): String {
        return when (val kind = address()) {
            AddressKind.Unnamed -> "(unnamed)"
            is AddressKind.Abstract -> "${AsciiEscaped(kind.name)} (abstract)"
            is AddressKind.Pathname -> "\"${kind.path}\" (pathname)"
        }
    }

    public companion object {
        internal fun new(f: (SockaddrUn, IntArray) -> Int): SocketAddr {
            val addr = SockaddrUn()
            val len = intArrayOf(SUN_PATH_LENGTH)
            cvt(f(addr, len))
            return fromParts(addr, len[0])
        }

        internal fun fromParts(addr: SockaddrUn, len: Int): SocketAddr {
            var actualLen = len
            if (actualLen == 0) {
                actualLen = sunPathOffset(addr)
            } else if (addr.sunFamily != AF_UNIX) {
                throw IllegalArgumentException("file descriptor did not correspond to a Unix socket")
            }

            return SocketAddr(addr, actualLen)
        }
    }
}

internal fun fromSockaddrUn(addr: SockaddrUn, len: Int): SocketAddr {
    return SocketAddr.fromParts(addr, len)
}

public fun fromPath(path: String): SocketAddr {
    val pair = sockaddrUn(path)
    return SocketAddr.fromParts(pair.first, pair.second)
}

private class AsciiEscaped(
    private val bytes: ByteArray,
) {
    override fun toString(): String {
        return buildString {
            append('"')
            for (byte in bytes) {
                append(byte.asciiEscapeDefault())
            }
            append('"')
        }
    }
}

private fun Byte.asciiEscapeDefault(): String {
    val value = toInt() and 0xff
    return when (value) {
        0x09 -> "\\t"
        0x0a -> "\\n"
        0x0d -> "\\r"
        0x22 -> "\\\""
        0x27 -> "\\'"
        0x5c -> "\\\\"
        in 0x20..0x7e -> value.toChar().toString()
        else -> "\\x" + value.toString(16).padStart(2, '0')
    }
}
