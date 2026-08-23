# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 2/5 (40.0%)
- **Function parity:** 11/106 matched (target 39) — 10.4%
- **Class/type parity:** 5/21 matched (target 11) — 23.8%
- **Combined symbol parity:** 16/127 matched (target 50) — 12.6%
- **Average inline-code cosine:** 1.00 (function body across 1 matched files)
- **Average documentation cosine:** 1.00 (doc text across 1 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 1 files with <0.60 function similarity

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
- **Priority Score:** 21810.0
- **Functions:** 11/13 matched (target 39)
- **Missing functions:** `fmt`, `eq`
- **Types:** 5/5 matched (target 10)
- **Missing types:** _none_

### 2. lib

- **Target:** `udswindows.Lib`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

