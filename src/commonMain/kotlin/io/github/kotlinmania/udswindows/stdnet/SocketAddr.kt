// port-lint: source stdnet/mod.rs
package io.github.kotlinmania.udswindows.stdnet

private const val SUN_PATH_LENGTH: Int = 108
internal const val SUN_PATH_OFFSET: Int = 2

internal const val AF_UNIX: Int = 1

internal class SockaddrUn(
    var sunFamily: Int = AF_UNIX,
    sunPath: ByteArray = ByteArray(SUN_PATH_LENGTH),
) {
    val sunPath: ByteArray = sunPath.copyOf(SUN_PATH_LENGTH)

    override fun equals(other: Any?): Boolean =
        other is SockaddrUn &&
            sunFamily == other.sunFamily &&
            sunPath.contentEquals(other.sunPath)

    override fun hashCode(): Int = 31 * sunFamily + sunPath.contentHashCode()

    override fun toString(): String =
        "sockaddr_un(sun_family=$sunFamily, sun_path=${decodeSunPathForDebug()})"

    private fun decodeSunPathForDebug(): String {
        val end = sunPath.firstNullIndex().takeIf { it >= 0 } ?: sunPath.size
        return sunPath.copyOfRange(0, end).decodeToString()
    }
}

internal fun sunPathOffset(addr: SockaddrUn): Int {
    require(addr.sunPath.size == SUN_PATH_LENGTH) { "sockaddr_un.sun_path must be $SUN_PATH_LENGTH bytes" }
    return SUN_PATH_OFFSET
}

/** Carries a raw Windows socket error code; constructed via [windowsSocketError]. */
internal class WindowsSocketException(
    val rawOsError: Int,
    message: String = "Windows socket error: $rawOsError",
) : RuntimeException(message)

internal fun windowsSocketError(code: Int): WindowsSocketException = WindowsSocketException(code)

/** Returns the last error from the Windows socket interface. */
internal fun defaultLastError(): WindowsSocketException =
    WindowsSocketException(-1, "Windows socket error: WSAGetLastError not available on this target")

internal fun interface IsMinusOne<T> {
    fun isMinusOne(value: T): Boolean
}

internal val ByteIsMinusOne: IsMinusOne<Byte> = IsMinusOne { it == (-1).toByte() }
internal val ShortIsMinusOne: IsMinusOne<Short> = IsMinusOne { it == (-1).toShort() }
internal val IntIsMinusOne: IsMinusOne<Int> = IsMinusOne { it == -1 }
internal val LongIsMinusOne: IsMinusOne<Long> = IsMinusOne { it == -1L }

/**
 * Checks if the signed integer is the Windows constant `SOCKET_ERROR` (-1)
 * and if so, throws the error returned by [lastError]. This function must be
 * called before another call to the socket API is made.
 */
internal inline fun <T> cvt(
    t: T,
    isMinusOne: IsMinusOne<T>,
    lastError: () -> Throwable = ::defaultLastError,
): T = if (isMinusOne.isMinusOne(t)) throw lastError() else t

internal fun cvt(t: Byte, lastError: () -> Throwable = ::defaultLastError): Byte =
    cvt(t, ByteIsMinusOne, lastError)

internal fun cvt(t: Short, lastError: () -> Throwable = ::defaultLastError): Short =
    cvt(t, ShortIsMinusOne, lastError)

internal fun cvt(t: Int, lastError: () -> Throwable = ::defaultLastError): Int =
    cvt(t, IntIsMinusOne, lastError)

internal fun cvt(t: Long, lastError: () -> Throwable = ::defaultLastError): Long =
    cvt(t, LongIsMinusOne, lastError)

internal fun sockaddrUn(path: String): Pair<SockaddrUn, Int> {
    require(path.none { it == '\u0000' }) { "paths may not contain interior null bytes" }
    val bytes = path.encodeToByteArray()
    require(bytes.size < SUN_PATH_LENGTH) { "path must be shorter than SUN_LEN" }

    val addr = SockaddrUn()
    bytes.copyInto(addr.sunPath, destinationOffset = 0, startIndex = 0, endIndex = bytes.size)

    var len = sunPathOffset(addr) + bytes.size
    if (bytes.isNotEmpty() && bytes.first() != 0.toByte()) {
        len += 1
    }
    return addr to len
}

internal sealed class AddressKind {
    data object Unnamed : AddressKind()

    data class Pathname(val path: String) : AddressKind()

    class Abstract(name: ByteArray) : AddressKind() {
        val name: ByteArray = name.copyOf()

        override fun equals(other: Any?): Boolean =
            other is Abstract && name.contentEquals(other.name)

        override fun hashCode(): Int = name.contentHashCode()
    }
}

/**
 * An address associated with a Unix socket.
 *
 * A pathname address can be inspected with [asPathname]. An unnamed address
 * reports true from [isUnnamed].
 */
class SocketAddr internal constructor(
    internal val addr: SockaddrUn,
    internal val len: Int,
) {
    init {
        require(len >= 0) { "address length must be non-negative" }
    }

    fun isUnnamed(): Boolean = address() is AddressKind.Unnamed

    fun asPathname(): String? =
        when (val kind = address()) {
            is AddressKind.Pathname -> kind.path
            else -> null
        }

    internal fun address(): AddressKind {
        val pathLen = len - sunPathOffset(addr)
        if (pathLen <= 0 || addr.sunPath.firstOrNull() == 0.toByte()) {
            return AddressKind.Unnamed
        }

        val boundedLen = pathLen.coerceIn(0, addr.sunPath.size)
        val pathBytes = addr.sunPath.copyOfRange(0, boundedLen)
        val nul = pathBytes.firstNullIndex()
        val pathnameBytes = if (nul >= 0) pathBytes.copyOfRange(0, nul) else pathBytes
        return AddressKind.Pathname(pathnameBytes.decodeToString())
    }

    override fun equals(other: Any?): Boolean =
        other is SocketAddr &&
            len == other.len &&
            addr.sunFamily == other.addr.sunFamily &&
            addr.sunPath.contentEquals(other.addr.sunPath)

    override fun hashCode(): Int = 31 * len + addr.hashCode()

    override fun toString(): String =
        when (val kind = address()) {
            AddressKind.Unnamed -> "(unnamed)"
            is AddressKind.Abstract -> "${AsciiEscaped(kind.name)} (abstract)"
            is AddressKind.Pathname -> "\"${kind.path}\" (pathname)"
        }

    internal companion object {
        fun fromParts(addr: SockaddrUn, len: Int): SocketAddr {
            val normalizedLen =
                if (len == 0) {
                    sunPathOffset(addr)
                } else {
                    len
                }

            require(normalizedLen == sunPathOffset(addr) || addr.sunFamily == AF_UNIX) {
                "file descriptor did not correspond to a Unix socket"
            }

            return SocketAddr(addr, normalizedLen)
        }
    }
}

internal fun fromSockaddrUn(addr: SockaddrUn, len: Int): SocketAddr =
    SocketAddr.fromParts(addr, len)

fun fromPath(path: String): SocketAddr {
    val (addr, len) = sockaddrUn(path)
    return SocketAddr.fromParts(addr, len)
}

private class AsciiEscaped(private val bytes: ByteArray) {
    override fun toString(): String =
        buildString {
            append('"')
            bytes.forEach { byte -> append(byte.asciiEscapeDefault()) }
            append('"')
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

private fun ByteArray.firstNullIndex(): Int {
    for (index in indices) {
        if (this[index] == 0.toByte()) {
            return index
        }
    }
    return -1
}
