// port-lint: source uds_windows/src/lib.rs
package io.github.kotlinmania.udswindows

public typealias SocketAddr = io.github.kotlinmania.udswindows.stdnet.SocketAddr
public typealias UnixStream = io.github.kotlinmania.udswindows.stdnet.UnixStream
public typealias UnixListener = io.github.kotlinmania.udswindows.stdnet.UnixListener
public typealias Incoming = io.github.kotlinmania.udswindows.stdnet.Incoming
public typealias AcceptAddrsBuf = io.github.kotlinmania.udswindows.stdnet.AcceptAddrsBuf
public typealias AcceptAddrs = io.github.kotlinmania.udswindows.stdnet.AcceptAddrs
public typealias Shutdown = io.github.kotlinmania.udswindows.stdnet.Shutdown

/**
 * Unix domain sockets for Windows.
 */
public object UdsWindowsLib {
    public const val MODULE_NAME: String = "udswindows"
    public const val CRATE_NAME: String = "udswindows"
}
