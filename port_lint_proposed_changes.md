# port-lint Proposed Changes

**Generated:** 2026-08-24
**Source:** tmp/uds_windows
**Target:** src

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `commonMain/kotlin/io/github/kotlinmania/udswindows/stdnet/Ext.kt` | `// port-lint: source stdnet/ext.rs` | `// port-lint: source stdnet/ext.rs` | `stdnet/ext.rs` | `port-lint provenance header matched only after fallback normalization: 'stdnet/ext.rs' vs expected 'stdnet/ext.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/udswindows/stdnet/Socket.kt` | `// port-lint: source stdnet/socket.rs` | `// port-lint: source stdnet/socket.rs` | `stdnet/socket.rs` | `port-lint provenance header matched only after fallback normalization: 'stdnet/socket.rs' vs expected 'stdnet/socket.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/udswindows/stdnet/Net.kt` | `// port-lint: source stdnet/net.rs` | `// port-lint: source stdnet/net.rs` | `stdnet/net.rs` | `port-lint provenance header matched only after fallback normalization: 'stdnet/net.rs' vs expected 'stdnet/net.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/udswindows/stdnet/NetTest.kt` | `// port-lint: tests stdnet/net.rs` | `// port-lint: tests stdnet/net.rs` | `stdnet/net.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:stdnet/net.rs' vs expected 'stdnet/net.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/udswindows/stdnet/Mod.kt` | `// port-lint: source stdnet/mod.rs` | `// port-lint: source stdnet/mod.rs` | `stdnet/mod.rs` | `port-lint provenance header matched only after fallback normalization: 'stdnet/mod.rs' vs expected 'stdnet/mod.rs'` |
| `commonTest/kotlin/io/github/kotlinmania/udswindows/stdnet/SocketAddrTest.kt` | `// port-lint: tests stdnet/mod.rs` | `// port-lint: tests stdnet/mod.rs` | `stdnet/mod.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:stdnet/mod.rs' vs expected 'stdnet/mod.rs'` |
| `commonMain/kotlin/io/github/kotlinmania/udswindows/Lib.kt` | `// port-lint: source lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'lib.rs' vs expected 'lib.rs'` |
