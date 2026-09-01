# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/5 (100.0%)
- **Function parity:** 46/88 matched (target 92) — 52.3%
- **Class/type parity:** 13/21 matched (target 24) — 61.9%
- **Combined symbol parity:** 59/109 matched (target 116) — 54.1%
- **Average inline-code cosine:** 0.19 (function body across 3 matched files)
- **Average documentation cosine:** 0.13 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 5 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. stdnet.ext

- **Target:** `stdnet.Ext`
- **Similarity:** 0.08
- **Dependents:** 0
- **Priority Score:** 212909.2
- **Functions:** 4/20 matched (target 4)
- **Missing functions:** `fmt`, `last_err`, `cvt`, `socket_addr_to_ptrs`, `ptrs_to_socket_addr`, `slice2buf`, `result`, `read_overlapped`, `write_overlapped`, `connect_overlapped`, `connect_complete`, `accept_overlapped`, `accept_complete`, `default`, `args`, `get`
- **Types:** 4/9 matched (target 4)
- **Missing types:** `WsaExtension`, `NetInt`, `ConnectEx`, `AcceptEx`, `GetAcceptExSockaddrs`

### 2. stdnet.socket

- **Target:** `stdnet.Socket`
- **Similarity:** 0.15
- **Dependents:** 0
- **Priority Score:** 152508.5
- **Functions:** 9/23 matched (target 12)
- **Missing functions:** `cvt_z`, `accept`, `recv_with_flags`, `read`, `write`, `set_no_inherit`, `setsockopt`, `getsockopt`, `drop`, `as_raw_socket`, `from_raw_socket`, `into_raw_socket`, `as_socket`, `from`
- **Types:** 1/2 matched
- **Missing types:** `IsZero`
- **Lint issues:** 1

### 3. stdnet.net

- **Target:** `stdnet.Net`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 123706.6
- **Functions:** 22/32 matched (target 47)
- **Missing functions:** `fmt`, `inner`, `flush`, `as_socket`, `as_raw_socket`, `from_raw_socket`, `into_raw_socket`, `into_iter`, `size_hint`, `tmpdir`
- **Types:** 3/5 matched (target 8)
- **Missing types:** `Item`, `IntoIter`
- **Tests:** 4/5 matched

### 4. stdnet.mod

- **Target:** `stdnet.Mod [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 21810.0
- **Functions:** 11/13 matched (target 29)
- **Missing functions:** `fmt`, `eq`
- **Types:** 5/5 matched (target 10)
- **Missing types:** _none_
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Matched

| Source | Target | Path |
|--------|--------|------|
| `uds_windows.lib` | `udswindows.Lib` | `uds_windows/src/lib` |

