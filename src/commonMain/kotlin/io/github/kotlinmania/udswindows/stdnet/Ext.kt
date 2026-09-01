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
}

public interface UnixStreamExt {
    public fun connectComplete(): Unit
}

public interface UnixListenerExt {
    public fun acceptComplete(socket: UnixStream): Unit
}
