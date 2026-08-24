# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/5 (100.0%)
- **Function parity:** 46/88 matched (target 100) — 52.3%
- **Class/type parity:** 13/21 matched (target 33) — 61.9%
- **Combined symbol parity:** 59/109 matched (target 133) — 54.1%
- **Average inline-code cosine:** 0.39 (function body across 4 matched files)
- **Average documentation cosine:** 0.34 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. stdnet.ext

- **Target:** `stdnet.Ext [PROVENANCE-FALLBACK]`
- **Similarity:** 0.08
- **Dependents:** 0
- **Priority Score:** 212909.2
- **Functions:** 4/20 matched (target 4)
- **Missing functions:** `fmt`, `last_err`, `cvt`, `socket_addr_to_ptrs`, `ptrs_to_socket_addr`, `slice2buf`, `result`, `read_overlapped`, `write_overlapped`, `connect_overlapped`, `connect_complete`, `accept_overlapped`, `accept_complete`, `default`, `args`, `get`
- **Types:** 4/9 matched (target 4)
- **Missing types:** `WsaExtension`, `NetInt`, `ConnectEx`, `AcceptEx`, `GetAcceptExSockaddrs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `stdnet/ext.rs` vs expected `stdnet/ext.rs`
- **Proposed provenance header:** `// port-lint: source stdnet/ext.rs` (current: `// port-lint: source stdnet/ext.rs`)
- **Lint issues:** 1

### 2. stdnet.socket

- **Target:** `stdnet.Socket [PROVENANCE-FALLBACK]`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 152508.5
- **Functions:** 9/23 matched (target 12)
- **Missing functions:** `cvt_z`, `accept`, `recv_with_flags`, `read`, `write`, `set_no_inherit`, `setsockopt`, `getsockopt`, `drop`, `as_raw_socket`, `from_raw_socket`, `into_raw_socket`, `as_socket`, `from`
- **Types:** 1/2 matched
- **Missing types:** `IsZero`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `stdnet/socket.rs` vs expected `stdnet/socket.rs`
- **Proposed provenance header:** `// port-lint: source stdnet/socket.rs` (current: `// port-lint: source stdnet/socket.rs`)
- **Lint issues:** 2

### 3. stdnet.net

- **Target:** `stdnet.Net [PROVENANCE-FALLBACK]`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 123706.6
- **Functions:** 22/32 matched (target 46)
- **Missing functions:** `fmt`, `inner`, `flush`, `as_socket`, `as_raw_socket`, `from_raw_socket`, `into_raw_socket`, `into_iter`, `size_hint`, `tmpdir`
- **Types:** 3/5 matched (target 8)
- **Missing types:** `Item`, `IntoIter`
- **Tests:** 4/5 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `stdnet/net.rs` vs expected `stdnet/net.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:stdnet/net.rs` vs expected `stdnet/net.rs`
- **Proposed provenance header:** `// port-lint: source stdnet/net.rs` (current: `// port-lint: source stdnet/net.rs`)
- **Proposed provenance header:** `// port-lint: tests stdnet/net.rs` (current: `// port-lint: tests stdnet/net.rs`)
- **Lint issues:** 2

### 4. stdnet.mod

- **Target:** `stdnet.Mod [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21810.0
- **Functions:** 11/13 matched (target 38)
- **Missing functions:** `fmt`, `eq`
- **Types:** 5/5 matched (target 11)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `stdnet/mod.rs` vs expected `stdnet/mod.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:stdnet/mod.rs` vs expected `stdnet/mod.rs`
- **Proposed provenance header:** `// port-lint: source stdnet/mod.rs` (current: `// port-lint: source stdnet/mod.rs`)
- **Proposed provenance header:** `// port-lint: tests stdnet/mod.rs` (current: `// port-lint: tests stdnet/mod.rs`)
- **Lint issues:** 2

### 5. lib

- **Target:** `udswindows.Lib [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source lib.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

