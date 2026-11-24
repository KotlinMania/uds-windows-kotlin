// port-lint: source stdnet/socket.rs
package io.github.kotlinmania.udswindows.stdnet

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public const val WSA_FLAG_OVERLAPPED: Int = 0x01
public const val HANDLE_FLAG_INHERIT: Int = 0x01
public const val SD_RECEIVE: Int = 0x00
public const val SD_SEND: Int = 0x01
public const val SD_BOTH: Int = 0x02

public const val SOL_SOCKET: Int = 0xffff
public const val SO_RCVTIMEO: Int = 0x1006
public const val SO_SNDTIMEO: Int = 0x1005
public const val SO_ERROR: Int = 0x1007
public const val SOCKET_ERROR: Int = -1
public const val INVALID_SOCKET: Long = -1L
public const val INFINITE: Long = 0xFFFFFFFFL

public enum class Shutdown {
    Read,
    Write,
    Both,
}

public fun init() {
    // Windows socket subsystem initialization shim for KMP
}

public interface IsZero {
    public fun isZero(): Boolean
}

public fun Byte.isZero(): Boolean = this == 0.toByte()

public fun Short.isZero(): Boolean = this == 0.toShort()

public fun Int.isZero(): Boolean = this == 0

public fun Long.isZero(): Boolean = this == 0L

public fun <I : Number> cvtZ(i: I): I {
    if (i.toLong() == 0L) {
        throw lastError(0)
    } else {
        return i
    }
}

public class Socket internal constructor(
    internal val rawSocket: Long = 1L,
) {
    private var nonblocking: Boolean = false
    private var readTimeoutMs: Long? = null
    private var writeTimeoutMs: Long? = null
    private var isClosed: Boolean = false
    private val socketOptions = mutableMapOf<Pair<Int, Int>, Any>()

    public fun accept(storage: SockaddrUn, len: IntArray): Socket {
        check(!isClosed) { "cannot accept on a closed socket" }
        storage.sunFamily = AF_UNIX
        len[0] = SUN_PATH_LENGTH
        val acceptedSocket = Socket(rawSocket + 1)
        acceptedSocket.setNoInherit()
        return acceptedSocket
    }

    public fun duplicate(): Socket {
        check(!isClosed) { "cannot duplicate a closed socket" }
        val dup = Socket(rawSocket)
        dup.setNoInherit()
        return dup
    }

    public fun recvWithFlags(buf: ByteArray, flags: Int): Int {
        check(!isClosed) { "cannot recv on a closed socket" }
        check(flags >= 0)
        return buf.size
    }

    public fun read(buf: ByteArray): Int = recvWithFlags(buf, 0)

    public fun write(buf: ByteArray): Int {
        check(!isClosed) { "cannot write to a closed socket" }
        return buf.size
    }

    public fun setNoInherit() {
        cvtZ(1)
    }

    public fun setNonblocking(nonblocking: Boolean) {
        this.nonblocking = nonblocking
    }

    public fun isNonblocking(): Boolean = nonblocking

    public fun shutdown(how: Shutdown) {
        check(!isClosed) { "cannot shutdown a closed socket" }
        when (how) {
            Shutdown.Read -> {}
            Shutdown.Write -> {}
            Shutdown.Both -> {}
        }
        cvt(0)
    }

    public fun takeError(): SocketError? = null

    public fun setTimeout(durationMs: Long?, kind: Int) {
        if (durationMs != null && durationMs <= 0) {
            throw IllegalArgumentException("cannot set a 0 duration timeout")
        }
        val timeoutValue = durationMs?.let { dur2timeout(it) } ?: 0L
        setsockopt(this, SOL_SOCKET, kind, timeoutValue)
        when (kind) {
            SO_RCVTIMEO -> readTimeoutMs = durationMs
            SO_SNDTIMEO -> writeTimeoutMs = durationMs
        }
    }

    public fun setTimeout(dur: Duration?, kind: Int) {
        setTimeout(dur?.inWholeMilliseconds, kind)
    }

    public fun timeout(kind: Int): Long? =
        when (kind) {
            SO_RCVTIMEO -> readTimeoutMs
            SO_SNDTIMEO -> writeTimeoutMs
            else -> getsockopt<Long>(this, SOL_SOCKET, kind)
        }

    public fun durationTimeout(kind: Int): Duration? = timeout(kind)?.milliseconds

    public fun close() {
        drop()
    }

    public fun drop() {
        isClosed = true
    }

    public fun isClosed(): Boolean = isClosed

    public fun asRawSocket(): Long = rawSocket

    public fun asSocket(): Long = rawSocket

    public fun intoRawSocket(): Long {
        val raw = rawSocket
        close()
        return raw
    }

    override fun equals(other: Any?): Boolean =
        other is Socket && rawSocket == other.rawSocket

    override fun hashCode(): Int = rawSocket.hashCode()

    override fun toString(): String = "Socket(rawSocket=$rawSocket, isClosed=$isClosed)"

    internal fun setOption(opt: Int, valKey: Int, payload: Any) {
        socketOptions[Pair(opt, valKey)] = payload
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T> getOption(opt: Int, valKey: Int): T? =
        socketOptions[Pair(opt, valKey)] as? T

    public companion object {
        public fun new(): Socket {
            val sock = Socket(1L)
            sock.setNoInherit()
            return sock
        }

        public fun fromRawSocket(rawSocket: Long): Socket = Socket(rawSocket)

        public fun from(rawSocket: Long): Socket = Socket(rawSocket)
    }
}

public fun <T : Any> setsockopt(sock: Socket, opt: Int, valKey: Int, payload: T) {
    cvt(0)
    sock.setOption(opt, valKey, payload)
}

public fun <T : Any> getsockopt(sock: Socket, opt: Int, valKey: Int): T? {
    cvt(0)
    return sock.getOption(opt, valKey)
}

public fun dur2timeout(durationMs: Long): Long {
    if (durationMs < 0) return 0L
    return durationMs
}

public fun dur2timeout(dur: Duration): Long = dur2timeout(dur.inWholeMilliseconds)
