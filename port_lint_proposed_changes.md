# port-lint Proposed Changes

**Generated:** 2026-08-31
**Source:** tmp/uds_windows/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/udswindows

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/udswindows/stdnet/Ext.kt` | `// port-lint: source uds_windows/src/stdnet/ext.rs` | `// port-lint: source stdnet/ext.rs` | `stdnet/ext.rs` | `port-lint provenance header matched only after fallback normalization: 'uds_windows/src/stdnet/ext.rs' vs expected 'stdnet/ext.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/udswindows/stdnet/Socket.kt` | `// port-lint: source uds_windows/src/stdnet/socket.rs` | `// port-lint: source stdnet/socket.rs` | `stdnet/socket.rs` | `port-lint provenance header matched only after fallback normalization: 'uds_windows/src/stdnet/socket.rs' vs expected 'stdnet/socket.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/udswindows/stdnet/Net.kt` | `// port-lint: source uds_windows/src/stdnet/net.rs` | `// port-lint: source stdnet/net.rs` | `stdnet/net.rs` | `port-lint provenance header matched only after fallback normalization: 'uds_windows/src/stdnet/net.rs' vs expected 'stdnet/net.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/udswindows/stdnet/NetTest.kt` | `// port-lint: tests uds_windows/src/stdnet/net.rs` | `// port-lint: tests stdnet/net.rs` | `stdnet/net.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:uds_windows/src/stdnet/net.rs' vs expected 'stdnet/net.rs'` |
