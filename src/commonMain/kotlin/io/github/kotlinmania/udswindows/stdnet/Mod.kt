package io.github.kotlinmania.udswindows.stdnet

// Module ledger for the upstream stdnet re-exports.
//
//   pub use self::ext::{AcceptAddrs, AcceptAddrsBuf, UnixListenerExt, UnixStreamExt};
//   pub use self::net::{UnixListener, UnixStream};
//
// Callers migrated: all upstream re-exports are used directly from their
// defining files — SocketAddr.kt, UnixListener.kt, UnixStream.kt, Ext.kt.

internal object StdnetMod
