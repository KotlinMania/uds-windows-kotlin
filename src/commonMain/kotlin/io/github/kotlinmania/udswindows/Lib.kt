// port-lint: source lib.rs
package io.github.kotlinmania.udswindows

// Unix domain sockets for Windows.
//
// The upstream crate conditionally exposes its Windows socket module from the
// crate root. Kotlin callers use the defining stdnet package directly while
// this file records that crate-root wiring without adding central alias APIs.
// The re-exported upstream names are fromPath, AcceptAddrs, AcceptAddrsBuf,
// SocketAddr, UnixListener, UnixListenerExt, UnixStream, and UnixStreamExt.
