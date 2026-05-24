# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/5 (40.0%)
- **Function parity:** 8/106 matched (target 25) — 7.5%
- **Class/type parity:** 4/21 matched (target 8) — 19.0%
- **Combined symbol parity:** 12/127 matched (target 33) — 9.4%
- **Average inline-code cosine:** 0.00 (function body across 0 matched files)
- **Average documentation cosine:** 0.00 (doc text across 0 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 2 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. stdnet.mod

- **Target:** `stdnet.Mod [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 61810.0
- **Functions:** 8/13 matched (target 25)
- **Missing functions:** `fmt`, `last_error`, `cvt`, `new`, `eq`
- **Types:** 4/5 matched (target 8)
- **Missing types:** `IsMinusOne`

### 2. lib

- **Target:** `udswindows.Lib [STUB]`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

