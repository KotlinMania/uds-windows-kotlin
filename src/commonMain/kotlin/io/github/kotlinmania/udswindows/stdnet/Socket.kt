// port-lint: source stdnet/socket.rs
package io.github.kotlinmania.udswindows.stdnet

public const val WSA_FLAG_OVERLAPPED: Int = 0x01
public const val HANDLE_FLAG_INHERIT: Int = 0x01
public const val SD_RECEIVE: Int = 0x00
public const val SD_SEND: Int = 0x01
public const val SD_BOTH: Int = 0x02

public const val SOL_SOCKET: Int = 0xffff
public const val SO_RCVTIMEO: Int = 0x1006
public const val SO_SNDTIMEO: Int = 0x1005
public const val SO_ERROR: Int = 0x1007

public enum class Shutdown {
    Read,
    Write,
    Both,
}

public fun init() {
    // Windows socket subsystem initialization shim for KMP
}

public class Socket internal constructor(
    internal val rawSocket: Long = 0L,
) {
    private var nonblocking: Boolean = false
    private var readTimeoutMs: Long? = null
    private var writeTimeoutMs: Long? = null
    private var isClosed: Boolean = false

    public fun duplicate(): Socket {
        check(!isClosed) { "cannot duplicate a closed socket" }
        return Socket(rawSocket)
    }

    public fun setNonblocking(nonblocking: Boolean) {
        this.nonblocking = nonblocking
    }

    public fun isNonblocking(): Boolean {
        return nonblocking
    }

    public fun shutdown(how: Shutdown) {
        check(!isClosed) { "cannot shutdown a closed socket" }
    }

    public fun takeError(): SocketError? {
        return null
    }

    public fun setTimeout(durationMs: Long?, kind: Int) {
        if (durationMs != null && durationMs <= 0) {
            throw IllegalArgumentException("cannot set a 0 or negative duration timeout")
        }
        when (kind) {
            SO_RCVTIMEO -> readTimeoutMs = durationMs
            SO_SNDTIMEO -> writeTimeoutMs = durationMs
        }
    }

    public fun timeout(kind: Int): Long? {
        return when (kind) {
            SO_RCVTIMEO -> readTimeoutMs
            SO_SNDTIMEO -> writeTimeoutMs
            else -> null
        }
    }

    public fun close() {
        isClosed = true
    }

    public fun isClosed(): Boolean {
        return isClosed
    }

    public companion object {
        public fun new(): Socket {
            return Socket(1L)
        }
    }
}

public fun dur2timeout(durationMs: Long): Long {
    if (durationMs < 0) return 0L
    return durationMs
}
