// port-lint: source stdnet/ext.rs
package io.github.kotlinmania.udswindows.stdnet

public class AcceptAddrsBuf {
    internal var localAddr: SocketAddr? = null
    internal var remoteAddr: SocketAddr? = null

    public fun parse(socket: UnixListener): AcceptAddrs =
        AcceptAddrs(
            local = localAddr ?: socket.localAddr(),
            remote = remoteAddr ?: fromPath(""),
        )

    override fun equals(other: Any?): Boolean =
        other is AcceptAddrsBuf &&
            localAddr == other.localAddr &&
            remoteAddr == other.remoteAddr

    override fun hashCode(): Int =
        31 * (localAddr?.hashCode() ?: 0) + (remoteAddr?.hashCode() ?: 0)

    override fun toString(): String =
        "AcceptAddrsBuf(local=$localAddr, remote=$remoteAddr)"

    public companion object {
        public fun new(): AcceptAddrsBuf = AcceptAddrsBuf()
    }
}

public class AcceptAddrs internal constructor(
    private val local: SocketAddr?,
    private val remote: SocketAddr?,
) {
    public fun local(): SocketAddr? = local

    public fun remote(): SocketAddr? = remote

    override fun equals(other: Any?): Boolean =
        other is AcceptAddrs &&
            local == other.local &&
            remote == other.remote

    override fun hashCode(): Int =
        31 * (local?.hashCode() ?: 0) + (remote?.hashCode() ?: 0)

    override fun toString(): String =
        "AcceptAddrs(local=$local, remote=$remote)"
}

public interface UnixStreamExt {
    public fun connectComplete(): Unit
}

public interface UnixListenerExt {
    public fun acceptComplete(socket: UnixStream): Unit
}
