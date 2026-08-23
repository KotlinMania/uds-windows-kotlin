// port-lint: source stdnet/mod.rs
package io.github.kotlinmania.udswindows.stdnet

// Module ledger for the upstream stdnet re-exports.
//
//   pub use self::ext::{AcceptAddrs, AcceptAddrsBuf, UnixListenerExt, UnixStreamExt};
//   pub use self::net::{UnixListener, UnixStream};
//
// Callers migrated: all upstream re-exports are used directly from their
// defining files — SocketAddr.kt, UnixListener.kt, UnixStream.kt, Ext.kt.

/**
 * Module descriptor for the stdnet module.
 */
public object StdnetMod {
    public const val MODULE_NAME: String = "stdnet"
    public const val CRATE_NAME: String = "uds_windows"
}
