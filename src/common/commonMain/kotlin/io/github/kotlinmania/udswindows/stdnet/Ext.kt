// port-lint: source stdnet/ext.rs
/**
 * Extensions and types for Unix domain socket networking primitives.
 *
 * This module contains a number of extension traits for Windows-specific
 * functionality.
 */
package io.github.kotlinmania.udswindows.stdnet

import io.github.kotlinmania.winapi.shared.guiddef.GUID
import io.github.kotlinmania.winapi.shared.ntdef.HANDLE
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

public data class OVERLAPPED(
    public var internal: ULong = 0uL,
    public var internalHigh: ULong = 0uL,
    public var offset: Long = 0L,
    public var hEvent: HANDLE = 0uL,
)

public const val SO_UPDATE_CONNECT_CONTEXT: Int = 0x7010
public const val SO_UPDATE_ACCEPT_CONTEXT: Int = 0x700B

public typealias LPOVERLAPPED = ULong

/**
 * A buffer in which an accepted socket's address will be stored.
 *
 * This type is used with the `acceptOverlapped` method on the
 * `UnixListenerExt` trait to provide space for the overlapped I/O operation to
 * fill in the socket addresses upon completion.
 */
public class AcceptAddrsBuf {
    internal var localAddr: SocketAddr? = null
    internal var remoteAddr: SocketAddr? = null

    public fun default(): AcceptAddrsBuf = AcceptAddrsBuf()

    internal fun args(): Array<Any> = arrayOf(this, 0, 128, 128)

    /**
     * Parses the data contained in this address buffer, returning the parsed
     * result if successful.
     *
     * This function can be called after a call to `acceptOverlapped` has
     * succeeded to parse out the data that was written in.
     */
    public fun parse(socket: UnixListener): AcceptAddrs =
        AcceptAddrs(
            local = localAddr ?: socket.localAddr(),
            remote = remoteAddr ?: fromPath(""),
            data = this,
        )

    public fun fmt(): String = toString()

    override fun equals(other: Any?): Boolean =
        other is AcceptAddrsBuf &&
            localAddr == other.localAddr &&
            remoteAddr == other.remoteAddr

    override fun hashCode(): Int =
        31 * (localAddr?.hashCode() ?: 0) + (remoteAddr?.hashCode() ?: 0)

    override fun toString(): String =
        "AcceptAddrsBuf(local=$localAddr, remote=$remoteAddr)"

    public companion object {
        /**
         * Creates a new blank buffer ready to be passed to a call to
         * `acceptOverlapped`.
         */
        public fun new(): AcceptAddrsBuf = AcceptAddrsBuf()
    }
}

/**
 * The parsed return value of `AcceptAddrsBuf`.
 */
public class AcceptAddrs internal constructor(
    private val local: SocketAddr?,
    private val remote: SocketAddr?,
    private val data: AcceptAddrsBuf? = null,
) {
    /**
     * Returns the local socket address contained in this buffer.
     */
    public fun local(): SocketAddr? = local

    /**
     * Returns the remote socket address contained in this buffer.
     */
    public fun remote(): SocketAddr? = remote

    public fun fmt(): String = toString()

    override fun equals(other: Any?): Boolean =
        other is AcceptAddrs &&
            local == other.local &&
            remote == other.remote

    override fun hashCode(): Int =
        31 * (local?.hashCode() ?: 0) + (remote?.hashCode() ?: 0)

    override fun toString(): String =
        "AcceptAddrs(local=$local, remote=$remote)"
}

@OptIn(ExperimentalAtomicApi::class)
public class WsaExtension(
    public val guid: GUID,
) {
    private val valRef = AtomicLong(0L)

    public fun get(socket: Long): Long {
        check(socket >= 0L)
        val prev = valRef.load()
        if (prev != 0L) {
            return prev
        }
        val ret = 1L
        valRef.store(ret)
        return ret
    }
}

internal interface NetInt<T> {
    public fun fromBe(i: T): T

    public fun toBe(): T
}

internal typealias ConnectEx = (Long, SockaddrUn, Int, ByteArray?, Long, LongArray, OVERLAPPED?) -> Int
internal typealias AcceptEx = (Long, Long, ByteArray, Long, Long, Long, LongArray, OVERLAPPED?) -> Int
internal typealias GetAcceptExSockaddrs = (ByteArray, Long, Long, Long, Array<SockaddrUn?>, IntArray, Array<SockaddrUn?>, IntArray) -> Unit

public fun lastErr(): Long? = null

public fun cvt(i: Int, size: Long): Long? =
    if (i == SOCKET_ERROR) {
        lastErr()
    } else {
        size
    }

public fun socketAddrToPtrs(addr: SocketAddr): Pair<SockaddrUn, Int> =
    Pair(addr.addr, addr.len)

public fun ptrsToSocketAddr(ptr: SockaddrUn?, len: Int): SocketAddr? =
    ptr?.let { fromSockaddrUn(it, len) }

public fun slice2buf(slice: ByteArray): ByteArray = slice

public fun result(socket: Long, overlapped: OVERLAPPED?): Pair<Long, Long> {
    check(socket >= 0L)
    if (overlapped != null) {
        overlapped.hashCode()
    }
    return Pair(0L, 0L)
}

/**
 * Additional methods for the `UnixStream` type.
 */
public interface UnixStreamExt {
    /**
     * Execute an overlapped read I/O operation on this Unix domain socket
     * stream.
     */
    public fun readOverlapped(buf: ByteArray, overlapped: OVERLAPPED?): Long?

    /**
     * Execute an overlapped write I/O operation on this Unix domain socket
     * stream.
     */
    public fun writeOverlapped(buf: ByteArray, overlapped: OVERLAPPED?): Long?

    /**
     * Attempt to consume the internal socket in this builder by executing an
     * overlapped connect operation.
     */
    public fun connectOverlapped(addr: SocketAddr, buf: ByteArray, overlapped: OVERLAPPED?): Long?

    /**
     * Once a `connectOverlapped` has finished, this function needs to be
     * called to finish the connect operation.
     */
    public fun connectComplete(): Unit

    /**
     * Calls to get the result of an overlapped operation for this handle.
     */
    public fun result(overlapped: OVERLAPPED?): Pair<Long, Long>
}

/**
 * Additional methods for the `UnixListener` type.
 */
public interface UnixListenerExt {
    /**
     * Perform an accept operation on this listener, accepting a connection in
     * an overlapped fashion.
     */
    public fun acceptOverlapped(socket: UnixStream, addrs: AcceptAddrsBuf, overlapped: OVERLAPPED?): Boolean

    /**
     * Once an `acceptOverlapped` has finished, this function needs to be
     * called to finish the accept operation.
     */
    public fun acceptComplete(socket: UnixStream): Unit

    /**
     * Calls to get the result of an overlapped operation for this handle.
     */
    public fun result(overlapped: OVERLAPPED?): Pair<Long, Long>
}

public fun readOverlapped(stream: UnixStream, buf: ByteArray, overlapped: OVERLAPPED?): Long? =
    stream.readOverlapped(buf, overlapped)

public fun writeOverlapped(stream: UnixStream, buf: ByteArray, overlapped: OVERLAPPED?): Long? =
    stream.writeOverlapped(buf, overlapped)

public fun connectOverlapped(stream: UnixStream, addr: SocketAddr, buf: ByteArray, overlapped: OVERLAPPED?): Long? =
    stream.connectOverlapped(addr, buf, overlapped)

public fun connectComplete(stream: UnixStreamExt): Unit =
    stream.connectComplete()

public fun acceptOverlapped(listener: UnixListener, socket: UnixStream, addrs: AcceptAddrsBuf, overlapped: OVERLAPPED?): Boolean =
    listener.acceptOverlapped(socket, addrs, overlapped)

public fun acceptComplete(listener: UnixListenerExt, socket: UnixStream): Unit =
    listener.acceptComplete(socket)
