# Build Targets & C-Binding Matrices (`BUILD_TARGETS.md`)

> **Scope & source-of-truth note.** [`AGENTS.md`](AGENTS.md) (§0, §5, §5.5, §10), [`build.gradle.kts`](build.gradle.kts), [`ANDROID.md`](ANDROID.md), [`SWIFT.md`](SWIFT.md), and [`verification/ALL_TARGET_VALIDATION.md`](verification/ALL_TARGET_VALIDATION.md) remain the normative workspace contracts and build-gate rules. This guide explains how the **17 retained KMP targets**, the **C/C++ FFI channels**, and **upstream Rust `cfg(...)` metadata** fit together so every repo can configure the right target matrix in [`gradle.properties`](gradle.properties) while keeping [`build.gradle.kts`](build.gradle.kts) 100% identical to `template`.

## Core Rule: Opt-Out Only, Zero Allowlists, Case-by-Case Semantics

Every retained workspace target defaults to `true` in [`build.gradle.kts`](build.gradle.kts#L133-L177). Allowlist properties (`project.targets`, `project.targets.exclude`) are forbidden by build-script guard (`build.gradle.kts:153-158`) so targets are never silently dropped.

To restrict a repo whose upstream Rust crate is OS-, device-, or VM-scoped:

1. Keep [`build.gradle.kts`](build.gradle.kts) byte-for-byte identical to `template`.
2. Disable inapplicable targets explicitly in [`gradle.properties`](gradle.properties) via `project.target.<name>=false` (plus `project.swiftExport.enabled=false` and `project.apple.framework.enabled=false` when Apple consumers do not apply).
3. Base every decision on **both** what upstream Rust supports **and** what the target runtime + C-binding layer can physically execute — never preserve impossible OS targets with `#else return 0` no-op C stubs.

---

## 1. The 17 Retained KMP Targets & Platform Capabilities

All **32-bit** targets (`watchosArm32`, `watchosArm64` (`arm64_32`), `linuxArm32Hfp`, `mingwX86`, `androidNativeArm32`, `androidNativeX86`) and **Apple x86_64** targets (`iosX64`, `macosX64`, `tvosX64`, `watchosX64`) are permanently retired workspace-wide (`AGENTS.md` §5.5) and must never be re-added.

The remaining **17 targets** split cleanly into **Desktop / Web VMs**, **Desktop Native**, and **Mobile / Embedded Devices**:

### 1.1 Capability Matrix (All 17 Retained Targets)

| Target (`project.target.*`) | Category | Runtime / ABI | JS / Node.js | JVM / ART | C-Binding Mechanism | Swift / XCFramework | Primary Validation Runner / Task |
|---|---|---|---|---|---|---|---|
| `jvm` | Desktop / Server VM | JDK 21 bytecode (`hostSystem` + JVM) | No | Host JVM | **JNI** (`clang -shared`, `$JAVA_HOME/include`) | No | `jvmTest` + `packagedJniTest` (macOS, Linux x64/ARM64, Windows x64) |
| `js` | Desktop / Web JS | JS IR (Node.js + Browser) | **Yes** | No | **Node N-API `.node`** (`NodeFfiBuilder`, Node only; none in browser) | No | `jsNodeTest`, `jsBrowserTest` |
| `wasmJs` | WebAssembly (JS host) | Wasm32 (JS/GC) on Node.js + Browser | **Yes** (JS host) | No | **Node N-API `.node`** via `@JsFun` → Node `createRequire` (none in browser) | No | `wasmJsNodeTest`, `wasmJsBrowserTest` |
| `wasmWasi` | WebAssembly (WASI VM) | Wasm32 (`wasi_snapshot_preview1`) on Node WASI | Node WASI runner only (no JS APIs) | No | **Sandboxed** (test-only `patchWasmWasiTestFfiImports` Node bridge; no host OS FFI in standalone WASI) | No | `wasmWasiNodeTest` (+ `project.wasmWasi.testPreopens`) |
| `macosArm64` | Desktop Native | Mach-O `arm64-apple-macos` | **No** | No | **`.def` cinterop** + `xcrun -sdk macosx` `.a` | `XCFramework` + **Swift Export host** | `macosArm64Test` + `swiftExportSmokeTest` |
| `linuxX64` | Desktop Native | ELF `x86_64-unknown-linux-gnu` | **No** | No | **`.def` cinterop** + Konan GCC sysroot `.a` | No | `linuxX64Test` / `validateLinuxX64Runtime` |
| `linuxArm64` | Desktop Native | ELF `aarch64-unknown-linux-gnu` | **No** | No | **`.def` cinterop** + Konan GCC sysroot `.a` | No | `linuxArm64Test` / `validateLinuxArm64Runtime` |
| `mingwX64` | Desktop Native | PE/COFF `x86_64-w64-mingw32` (`-Wl,--stack,8388608`) | **No** | No | **`.def` cinterop** + Konan `msys2-mingw-w64-x86_64-2` `.a` | No | `mingwX64Test` / `validateMingwX64Runtime` |
| `android` | **Device VM** (AAR) | ART bytecode + AAR `jniLibs` (`arm64-v8a`, `x86_64`) | **No** | **Yes** (ART + host unit JVM) | **NDK JNI `.so`** (`compileAndroidJni`, **not** `.def` cinterop) | No | `testAndroidHostTest` + `connectedAndroidDeviceTest` / `verifyAndroidDeviceTestResults` |
| `androidNativeArm64` | **Device Native** (NDK) | ELF `aarch64-linux-android` (Bionic, PAC/BTI) | **No** | No | **`.def` cinterop** + NDK `clang` `.a` (`-mbranch-protection=standard`) | No | `validateAndroidNativeArm64Runtime` (`adb`) |
| `androidNativeX64` | **Device Native** (NDK) | ELF `x86_64-linux-android` (Bionic) | **No** | No | **`.def` cinterop** + NDK `clang` `.a` | No | `validateAndroidNativeX64Runtime` (`adb` emulator) |
| `iosArm64` | **Device Native** | Mach-O `arm64-apple-ios` | **No** | No | **`.def` cinterop** + `xcrun -sdk iphoneos` `.a` | `XCFramework` | `iosArm64TestBinaries` (device harness needed to execute) |
| `iosSimulatorArm64` | **Device Simulator** | Mach-O `arm64-apple-ios-simulator` | **No** | No | **`.def` cinterop** + `xcrun -sdk iphonesimulator` `.a` | `XCFramework` | `iosSimulatorArm64Test` |
| `tvosArm64` | **Device Native** | Mach-O `arm64-apple-tvos` | **No** | No | **`.def` cinterop** + `xcrun -sdk appletvos` `.a` | `XCFramework` | `tvosArm64TestBinaries` (device harness needed to execute) |
| `tvosSimulatorArm64` | **Device Simulator** | Mach-O `arm64-apple-tvos-simulator` | **No** | No | **`.def` cinterop** + `xcrun -sdk appletvsimulator` `.a` | `XCFramework` | `tvosSimulatorArm64Test` |
| `watchosDeviceArm64` | **Device Native** | Mach-O `arm64-apple-watchos` | **No** | No | **`.def` cinterop** + `xcrun -sdk watchos` `.a` | `XCFramework` | `watchosDeviceArm64TestBinaries` (device harness needed to execute) |
| `watchosSimulatorArm64` | **Device Simulator** | Mach-O `arm64-apple-watchos-simulator` | **No** | No | **`.def` cinterop** + `xcrun -sdk watchsimulator` `.a` | `XCFramework` | `watchosSimulatorArm64Test` |

> **Key boundary — Devices vs. JavaScript / JNI / `.def`:**
>
> - **Devices (`android`, `androidNative*`, `ios*`, `tvos*`, `watchos*`) have no JavaScript or Node.js runtime.** Device-only crates (`android-system-properties-kotlin`, Apple framework bindings) never build `js`, `wasmJs`, or `wasmWasi`.
> - **`android` ≠ `androidNative*` (see [`ANDROID.md`](ANDROID.md)).**
>   - `android` produces a JVM/ART Android Library (`.aar`) where C bindings go through **JNI** (`compileAndroidJni` compiling `src/jvm/jvmMain/c/*.c` into `arm64-v8a` and `x86_64` `.so` libraries packaged under `jniLibs/`).
>   - `androidNativeArm64` and `androidNativeX64` are **Kotlin/Native ELF** targets compiled directly against Bionic that leverage **`.def` files (cinterop)** and NDK-compiled static `.a` archives.
> - **Swift Export is a language boundary, not an 18th KMP target.** It activates when `targetEnabled("macosArm64")` and `project.swiftExport.enabled != false` (`build.gradle.kts:277-279`), running Debug + Release SPM verification via `swiftExportSmokeTest`. `project.apple.framework.enabled` controls the Apple `XCFramework`.

---

## 2. C/C++ Binding Channels (`project.ffi.*`) × Targets

Three independent FFI gates in [`gradle.properties`](gradle.properties#L161-L174) wire C/C++ native code in [`build.gradle.kts`](build.gradle.kts#L227-L243) and [`buildSrc/.../FfiBuildSupport.java`](buildSrc/src/main/java/io/github/kotlinmania/build/FfiBuildSupport.java).

### 2.1 Channel Summary

| Channel Property | Target Scope | Source Inputs | Build Tool & Flags (`template` `982d142`) | Output Artifacts |
|---|---|---|---|---|
| `project.ffi.native=true` | All 12 `KotlinNativeTarget`s (narrowable via `project.ffi.native.targets`) | `src/native/interop/cinterop/*.def` (`<Name>.def`), `*_wrapper.{h,c,cpp}`, optional arch-suffixed `*.S`/`*.s` | `cinterop` tool; if `project.ffi.native.compileWrapper=true`, compiles `.a` via `xcrun -sdk <sdk> clang`, Konan Android NDK clang (`-mbranch-protection=standard` on `arm64`), Konan Linux GCC sysroot + `clang`, or Konan `msys2-mingw-w64-x86_64-2` + `clang --target=x86_64-w64-mingw32` (`-O0 -g` Debug / `-O2 -DNDEBUG` Release) | `build/cinterop-targets/<konanTarget>/{debug,release}/<lower>_wrapper.a` (Release staged to `cinterop-targets/<konanTarget>/`); prebuilt fallback under `src/native/interop/cinterop/targets/<konan_target>/` |
| `project.ffi.jvm=true` | `jvm` (host JNI) + `android` (`compileAndroidJni`, NDK `.so` when `android` is enabled) | `src/jvm/jvmMain/c/*.c` (and `*.S`/`*.s` across `jvmMain/c` + `cinterop/`) | **Host JVM:** `clang -shared` against `$JAVA_HOME/include/{darwin,linux,win32}` (`-O0 -g` / `-O2 -DNDEBUG`, `-fPIC` & `-fvisibility=hidden` on Unix, `-lws2_32` on Windows).<br>**Android (`androidTargetEnabled`):** Konan NDK clang (`arm64-v8a` w/ `-mbranch-protection=standard`, `x86_64`) with `-shared -fPIC -fvisibility=hidden -Wall -Wextra -Werror -Wl,-z,max-page-size=16384` | **Host JVM:** `build/natives/<lib>`, packaged by `package<Name>Jni` under `<ns>/native/<os>-<arch>/` in `jvmJar`.<br>**Android:** `build/android-jni/{arm64-v8a,x86_64}/lib<lower>_jni.so` → AAR `jniLibs` |
| `project.ffi.napi=true` | `js` (`jsNodeTest`) + `wasmJs` (`wasmJsNodeTest`; also bridges `wasmWasiNodeTest` tests if enabled) | `src/js/node-<lower>/dist/{binding.gyp,package.json,index.js}` + `src/js/node-<lower>/src/*_bindings.cpp` | `NodeFfiBuilder` compiles on the active host (`cl.exe`/`clang-cl.exe` on Windows; `cc`/`c++` on macOS/Linux) with verified Node release headers, `node-addon-api`, `DEBUG` vs `NDEBUG`, and `binding.gyp` `OS`/`target_arch` conditions | `src/js/node-<lower>/dist/build/{Debug,Release}/<product>.node` copied to `build/{js,wasm}/node_modules/@kotlinmania/<lower>-native-bindings` |

Additional `project.ffi.native` details verified in [`build.gradle.kts`](build.gradle.kts#L809-L891):
- **`.def` discovery:** `build.gradle.kts` auto-discovers every `*.def` under `src/native/interop/cinterop/` (sorted by filename), defaulting each cinterop's Kotlin `packageName` to an explicit `package = ...` inside the `.def` or `<lower>.cinterop`.
- **Commonization:** `kotlin.mpp.enableCInteropCommonization=true` in `gradle.properties` allows shared `nativeMain`, `posixMain`, `appleMain`, `linuxMain`, `androidNativeMain`, and `mingwMain` actuals to import generated cinterop packages directly (`AGENTS.md` §10).
- **`.def`-only bindings (`project.ffi.native.compileWrapper=false`):** Skips `compile<Name>Wrapper*` when binding directly to system libraries, Apple SDK frameworks, or NDK headers via `.def` alone (`gradle.properties:170-171`).
- **Disabling unused JNI / N-API Kotlin sources:** When `project.ffi.jvm=false` or `project.ffi.napi=false`, `project.ffi.jvm.kotlinExcludes` / `project.ffi.napi.kotlinExcludes` / `project.ffi.napi.kotlinSources` exclude bridge files cleanly from compilation (`build.gradle.kts:929-960`).

### 2.2 Target × FFI Channel Cross-Reference

| Target | `project.ffi.native` (`.def` / `.a`) | `project.ffi.jvm` (JNI `.so`/`.dylib`/`.dll`) | `project.ffi.napi` (Node `.node`) | Browser / WASI Caveats |
|---|---|---|---|---|
| `jvm` | — | **Yes** (`compile<Name>Jni`, `package<Name>Jni`, `packagedJniTest`) | — | Runs on host JVM (`macos-aarch64`, `linux-x86_64`, `linux-aarch64`, `windows-x86_64`) |
| `android` | — *(never uses `.def`)* | **Yes** (`compileAndroidJni` → AAR `jniLibs`) | — *(no Node/JS on device)* | `androidHostTest` runs on dev host JVM; `androidDeviceTest` runs on ART/Bionic |
| `androidNativeArm64`, `androidNativeX64` | **Yes** (`.def` cinterop + NDK `.a` vs Bionic) | — | — *(no Node/JS on device)* | Compiled as standalone ELF executables / sharedLibs |
| `macosArm64`, `ios*`, `tvos*`, `watchos*` | **Yes** (`.def` cinterop + `xcrun` SDK `.a`) | — | — *(no Node/JS on mobile/TV/watch)* | Apple simulators/devices run K/N binaries; `macosArm64` also drives Swift Export |
| `linuxX64`, `linuxArm64` | **Yes** (`.def` cinterop + GCC sysroot `.a`) | — | — | Tested on Linux runners or Parallels guests |
| `mingwX64` | **Yes** (`.def` cinterop + MinGW sysroot `.a`) | — | — | Tested on Windows runners or Parallels Windows guest |
| `js` | — | — | **Yes** (`jsNodeTest` via `@JsModule` or Node `createRequire`) | **Browsers cannot load `.node`**: keep browser-safe fallback/guard + add FFI tests to `project.js.browserTestExcludes` |
| `wasmJs` | — | — | **Node only** (`wasmJsNodeTest` via `@JsFun` → Node `createRequire`) | Same VM/browser limit: `wasmJsBrowserTest` has no Node `process`/`require` |
| `wasmWasi` | — *(or custom C→Wasm, no host OS FFI)* | — | **Test harness bridge only** (`patchWasmWasiTestFfiImports`) | Isolated WASI VM (`wasi_snapshot_preview1`); standalone WASI engines have no OS hardware access; use `project.wasmWasi.testPreopens` for virtual filesystem tests |

---

## 3. How the Matrices Work Together — Decision Rules

When evaluating a crate's target matrix, combine **upstream Rust metadata (`tmp/`)** with the **runtime & C-binding constraints** above in three steps:

### Step 1: Read upstream Rust (`tmp/`) first

Check:
- **`tmp/<crate>/src/lib.rs` (top attributes):** `#![cfg(windows)]`, `#![cfg(unix)]`, `#![cfg(target_os = "...")]`, `#![cfg(target_endian = "...")]`.
- **`tmp/<crate>/Cargo.toml`:**
  - `[package.metadata.docs.rs]` (`default-target`, `targets`)
  - `categories` (`os::windows-apis`, `os::macos-apis`, `os::linux-apis`, `embedded`, etc.)
  - `[target.'cfg(...)'.dependencies]` (whether macOS, iOS, tvOS, watchOS, Android, Linux, Windows, or `wasm32` have distinct dependency graphs)

### Step 2: Intersect Rust OS support with Kotlin target & C-binding reality

1. **Never keep an OS target backed by fake `#else return 0` no-op C stubs.**
   If the crate is `#![cfg(windows)]` (e.g., Win32 bindings), building Android AARs, Apple XCFrameworks, Swift Export, or Linux binaries only tests `#else` stubs that do not call Windows. Opt those OS targets out.
2. **Distinguish cross-platform FFI from single-OS host FFI (`jvm` and `js`):**
   - **Cross-platform FFI (`libc-kotlin`):** `jvm` (JNI) and `js` (N-API) link real C libraries on macOS, Linux, and Windows, so `jvm.yml` and `js.yml` legitimately fan out across all 4 desktop hosts (`macosArm64`, `linuxX64`, `linuxArm64`, `mingwX64`).
   - **OS-specific host FFI (`winapi-kotlin`, `windows-sys-kotlin`):** JVM and Node.js *do* run on Windows, so `jvm` and `js` belong in the target matrix when Java/JS consumers on Windows need those bindings — **built and tested on the Windows runner (`mingwX64` / `windows-latest`)**, not via `#else` stubs on macOS or Linux.
3. **Evaluate WebAssembly (`wasmJs` vs. `wasmWasi`) under VM isolation:**
   - Wasm runs inside its own sandboxed VM: even when hosted on Windows, Wasm code cannot directly invoke OS syscalls or talk to OS hardware.
   - **`wasmWasi` (`wasi_snapshot_preview1`):** Keep for pure-Kotlin logic and WASI-supported I/O (preopened files/clocks/stdio), and **drop** for hardware-only or OS-native crates (`winapi-kotlin`, `windows-sys-kotlin`, `core-foundation-kotlin`, `android-system-properties-kotlin`).
   - **`wasmJs`:** For pure computation crates, keeping `wasmJs` costs little and runs cleanly in Node + browser (and only needs one Wasm test runner). For OS-native FFI crates, `wasmJs` can only call the OS by shelling out through Node.js to the exact same N-API `.node` addon as `js`, while failing in browsers — so Windows-only `windows-sys-kotlin` drops both `wasmJs` and `wasmWasi` (unless a project explicitly needs a single canonical Node-hosted Wasm bridge).
4. **Device targets stay device-scoped:**
   - Device-only crates (Android system properties, Apple UIKit/Metal/Foundation/CoreFoundation) should **drop `js`, `wasmJs`, and `wasmWasi`** (devices do not run JS/Node).
   - For Android-only crates, keep `android` (ART AAR + optional JNI `.so`) paired with `androidNativeArm64` + `androidNativeX64` (NDK `.def` cinterop) as appropriate (`android-system-properties-kotlin`).
   - For Apple-only crates (`os::macos-apis` / `objc2-*`), match the Apple OS set declared in `Cargo.toml` (`macosArm64` always for Swift Export when Swift APIs make sense, plus `ios*`, `tvos*`, `watchos*` only where the upstream Apple framework exists — e.g., macOS-only frameworks keep `macosArm64` and disable `ios*`/`tvos*`/`watchos*`).

### Step 3: Check downstream & upstream dependencies (`libs.versions.toml` / `settings.gradle.kts`)

Kotlin Multiplatform requires every `commonMain` dependency to publish metadata compatible with all targets enabled in the downstream consumer:
- Disabling a target on an upstream binding crate means downstream consumers using it in `commonMain` must either apply the same target opt-outs (e.g., `uds-windows-kotlin` depending on `winapi-kotlin`) or move that dependency to a narrower source set via `project.dependencies.sourceSet.<sourceSet>=<alias>`.
- Conversely, pure-Kotlin utility crates (`anyhow-kotlin`, `semver-kotlin`, `uuid-kotlin`) keep all 17 targets enabled so any downstream profile can depend on them from `commonMain`.

---

## 4. Canonical Crate Profiles & Ready-to-Use `gradle.properties` Recipes

| Profile | Kept Targets | Opted-Out Targets | C-Binding Pattern | Representative Workspace Crates |
|---|---|---|---|---|
| **1. Pure portable crate** (default) | All 17 | None | None (`project.ffi.*=false`) | `semver-kotlin`, `anyhow-kotlin`, `uuid-kotlin` |
| **2. Cross-platform FFI foundation** | All 17 | None | `native=true` (`.def`/POSIX), `jvm=true` (desktop + Android JNI), `napi=true` (`js` + `wasmJs` Node; `patchWasmWasiTestFfiImports` for WASI tests) | `libc-kotlin`, `rust-lang-kotlin` |
| **3. Windows-only (`#![cfg(windows)]`)** | `mingwX64`, `jvm`, `js` *(or omit `jvm`/`js` if K/N-only)* | All 14 non-Windows (`android`, `androidNative*`, all 7 Apple, `linux*`, `wasmJs`, `wasmWasi`) + `swiftExport=false`, `apple.framework=false` | `mingwX64`: `.def` cinterop (`win32`/MSVC `.a`); `jvm`: Windows JNI `.dll`; `js`: Windows N-API `.node` | `windows-sys-kotlin`, `winapi-kotlin`, `windows-kotlin`, `uds-windows-kotlin`, `winapi-util-kotlin`, `anstyle-wincon-kotlin` |
| **4. Android-only** | `android`, `androidNativeArm64`, `androidNativeX64` | All 14 non-Android (`jvm`, all 7 Apple, `linux*`, `mingwX64`, `js`, `wasmJs`, `wasmWasi`) | `android`: NDK JNI `.so`; `androidNative*`: `.def` cinterop vs Bionic + PAC/BTI | `android-system-properties-kotlin` |
| **5. Apple-only (`os::macos-apis` / Apple frameworks)** | `macosArm64` + applicable `ios*` / `tvos*` / `watchos*` *(optional `jvm`/`js` only if macOS host JNI/N-API are shipped)* | `android`, `androidNative*`, `linux*`, `mingwX64`, `wasm*` (and `jvm`/`js`, plus unavailable iOS/tvOS/watchOS slices) | `.def` cinterop (`project.ffi.native.compileWrapper=false` for Apple system frameworks) + `XCFramework` + Swift Export (when `macosArm64` enabled) | `core-foundation-kotlin`, `core-foundation-sys-kotlin`, `objc2-foundation-kotlin`, `objc2-metal-kotlin` |
| **6. Linux-only (`os::linux-apis`)** | `linuxX64`, `linuxArm64` *(plus `jvm`/`js` if Linux JNI/N-API exist; `android`/`androidNative*` only if Bionic/SELinux allow the Linux API)* | All Apple, `mingwX64`, `wasm*`, `android*` (unless Bionic-supported), and `jvm`/`js` if K/N-only | `.def` cinterop (Linux kernel/UAPI headers + Konan GCC sysroot `.a`); optional Linux JNI `.so` / Node `.node` | `landlock-kotlin`, `seccompiler-kotlin` |
| **7. Host CLI / native tool** | Host desktop native target(s) + optional `jvm` library consumer | Mobile/device & Wasm/JS targets (`15` opt-outs for macOS+JVM example) | `project.native.executable.entryPoint=...` + `stageNativeCliBinary` on `macosArm64` | `rust-kotlin` |

### Copy-Paste `gradle.properties` Opt-Out Blocks

<details>
<summary>Profile 3: Windows-only (<code>mingwX64</code>, <code>jvm</code>, <code>js</code> — verified in <code>windows-sys-kotlin</code>)</summary>

```properties
project.target.android=false
project.target.macosArm64=false
project.target.iosArm64=false
project.target.iosSimulatorArm64=false
project.target.tvosArm64=false
project.target.tvosSimulatorArm64=false
project.target.watchosDeviceArm64=false
project.target.watchosSimulatorArm64=false
project.target.linuxX64=false
project.target.linuxArm64=false
project.target.androidNativeArm64=false
project.target.androidNativeX64=false
project.target.wasmJs=false
project.target.wasmWasi=false
project.swiftExport.enabled=false
project.apple.framework.enabled=false
```

</details>

<details>
<summary>Profile 4: Android-only (<code>android</code>, <code>androidNativeArm64</code>, <code>androidNativeX64</code> — verified in <code>android-system-properties-kotlin</code>)</summary>

```properties
project.target.jvm=false
project.target.macosArm64=false
project.target.iosArm64=false
project.target.iosSimulatorArm64=false
project.target.tvosArm64=false
project.target.tvosSimulatorArm64=false
project.target.watchosDeviceArm64=false
project.target.watchosSimulatorArm64=false
project.target.linuxX64=false
project.target.linuxArm64=false
project.target.mingwX64=false
project.target.js=false
project.target.wasmJs=false
project.target.wasmWasi=false
```

</details>

<details>
<summary>Profile 5: Apple-only (macOS + iOS + tvOS + watchOS; drop unused mobile OSes if macOS-only)</summary>

```properties
project.target.jvm=false
project.target.android=false
project.target.linuxX64=false
project.target.linuxArm64=false
project.target.mingwX64=false
project.target.androidNativeArm64=false
project.target.androidNativeX64=false
project.target.js=false
project.target.wasmJs=false
project.target.wasmWasi=false
# If the Apple framework is macOS-only (e.g., AppKit / macOS core-foundation APIs), also set:
# project.target.iosArm64=false
# project.target.iosSimulatorArm64=false
# project.target.tvosArm64=false
# project.target.tvosSimulatorArm64=false
# project.target.watchosDeviceArm64=false
# project.target.watchosSimulatorArm64=false
```

</details>

<details>
<summary>Profile 6: Linux-only (<code>linuxX64</code>, <code>linuxArm64</code>; remove <code>jvm</code>/<code>js</code> opt-outs if Linux JNI/N-API are provided)</summary>

```properties
project.target.jvm=false
project.target.android=false
project.target.macosArm64=false
project.target.iosArm64=false
project.target.iosSimulatorArm64=false
project.target.tvosArm64=false
project.target.tvosSimulatorArm64=false
project.target.watchosDeviceArm64=false
project.target.watchosSimulatorArm64=false
project.target.mingwX64=false
project.target.androidNativeArm64=false
project.target.androidNativeX64=false
project.target.js=false
project.target.wasmJs=false
project.target.wasmWasi=false
project.swiftExport.enabled=false
project.apple.framework.enabled=false
```

</details>

---

## 5. Source-Set Hierarchy, Folder Layout & Automatic Cascades

### 5.1 KMP Hierarchy & `IdeSourceSetSupport` Paths

[`build.gradle.kts`](build.gradle.kts#L496-L516) applies Kotlin's default hierarchy template augmented with `hostSystem`, `web`, and `posix`, and [`IdeSourceSetSupport.java`](buildSrc/src/main/java/io/github/kotlinmania/build/IdeSourceSetSupport.java#L16-L98) maps each source set to `src/`:

```text
common                                  -> src/common/common{Main,Test}
├── wasmWasi                            -> src/js/wasm/wasmWasi{Main,Test}   (outside hostSystem & web: sandboxed VM)
└── hostSystem                          -> src/common/hostSystem{Main,Test}
    ├── jvm                             -> src/jvm/jvm{Main,Test}            (+ src/jvm/jvmMain/c/*.c for JNI)
    ├── android                         -> src/android/jvm/androidMain, androidHostTest, androidDeviceTest
    ├── web                             -> src/js/web{Main,Test}
    │   ├── js                          -> src/js/js{Main,Test}              (+ src/js/node-<lower>/ for N-API)
    │   └── wasmJs                      -> src/js/wasm/wasmJs{Main,Test}
    └── native                          -> src/native/native{Main,Test}      (+ src/native/interop/cinterop/*.def)
        ├── mingw -> mingwX64           -> src/native/mingw/mingw{Main,X64Main,Test,X64Test}
        └── posix                       -> src/native/posix/posix{Main,Test}
            ├── apple                   -> src/apple/{apple,macos,ios,tvos,watchos}/...
            ├── linux                   -> src/native/linux/linux{Main,X64Main,Arm64Main,...}
            └── androidNative           -> src/android/native/androidNative{Main,Arm64Main,X64Main,...}
```

### 5.2 What Cascades Automatically When a Target Is Disabled

Disabling a target via `project.target.<name>=false` automatically suppresses every downstream mechanism in [`build.gradle.kts`](build.gradle.kts):

- **Android AGP plugin:** Only applied when `project.target.android != false` (`build.gradle.kts:468-475`).
- **Local `.android-sdk` setup:** Only runs when `android` OR `androidNativeArm64` OR `androidNativeX64` is enabled (`build.gradle.kts:476-478`).
- **Node/Yarn/Webpack extensions & lockfile checks:** Configured separately for `jsTargetEnabled` (`kotlinNodeJs` / `kotlinYarn`) and `anyWasmTargetEnabled` (`kotlinWasmNodeJs` / `kotlinWasmYarn`) (`build.gradle.kts:1185-1294, 1444-1454`).
- **Swift Export & XCFramework:** Swift Export disables automatically whenever `macosArm64=false` OR `project.swiftExport.enabled=false` (`build.gradle.kts:277-279`); `assemble<Framework>XCFramework` and `hasAppleTargets` disable automatically when all 7 Apple targets are `false` (or `project.apple.framework.enabled=false`).
- **Gate task sets:** `fullTargetBuildTaskNames`, `debugBuildTaskNames`, `releaseBuildTaskNames`, `publicationRuntimeTaskNames`, `foreignNativeRuntimeTasks`, and `androidNativeRuntimeTasks` all filter cleanly from `enabledTargets` so `./gradlew build`, `./gradlew test`, and `publicationCheck` never fail expecting disabled targets.

### 5.3 Target & FFI Property Reference (`gradle.properties`)

| Property | Default | Effect |
|---|---|---|
| `project.target.<name>` | `true` for all 17 `allSupportedTargets` | Set `false` to opt a target out; any value other than `true`/`false` or unknown target fails configuration (`build.gradle.kts:163-171`) |
| `project.ffi.native` / `jvm` / `napi` | `false` | Enables cinterop + `.a` wrapper, host/Android JNI, and Node N-API addon pipelines |
| `project.ffi.name` | `Ffi` | PascalCase stem for tasks, `<Name>.def`, `<lower>_wrapper.{h,c}`, `<lower>_jni.c`, `src/js/node-<lower>/`, `@kotlinmania/<lower>-native-bindings` |
| `project.ffi.native.targets` | empty (all enabled K/N targets) | CSV subset of enabled `KotlinNativeTarget` names receiving cinterop + wrappers (`build.gradle.kts:228, 811-825`) |
| `project.ffi.native.compileWrapper` | `true` | Set `false` for `.def`-only bindings to system libraries or Apple frameworks |
| `project.ffi.jvm.kotlinExcludes` / `napi.kotlinExcludes` / `napi.kotlinSources` | template patterns / empty | Excludes FFI Kotlin bridges when `project.ffi.jvm=false` or `project.ffi.napi=false` |
| `project.ffi.jvm.hosts` | empty (derived from enabled desktop native targets) | Explicit CSV of required JNI payload slugs (`macos-aarch64`, `linux-x86_64`, `linux-aarch64`, `windows-x86_64`) checked by `verifyJniDistribution` |
| `project.ffi.jvm.windowsLibraries` | empty (`-lws2_32` always linked) | Extra Windows JNI link libraries appended to `clang -shared` (e.g., `oleaut32,user32,advapi32`) |
| `project.dependencies.sourceSet.<ssName>` | none | Attaches version-catalog aliases cleanly to one source set (`jvmMain`, `mingwX64Main`, `nativeMain`, etc.) instead of `commonMain` (`build.gradle.kts:1023-1048`) |
| `project.js.browserTestExcludes` | empty | Excludes FFI-only test classes/methods from `jsBrowserTest` and `wasmJsBrowserTest` |
| `project.wasmWasi.testPreopens`, `project.wasmWasi.windowsTestExcludes` | empty | Host-directory preopens and Windows WASI test exclusions for `wasmWasiNodeTest` |
| `project.swiftExport.enabled`, `project.apple.framework.enabled` | `true` (when Apple/`macosArm64` active) | Explicitly disables Swift Export SPM smoke tests or `XCFramework` packaging |

---

## 6. CI Workflow Scoping & Remaining Template Plumbing Notes

### 6.1 CI Matrix Scoping via `.github/workflows/ci.yml`

[`ci.yml`](.github/workflows/ci.yml#L1-L188) exposes `target`, `platform`, and `phase` inputs so reusable workflows ([`jvm.yml`](.github/workflows/jvm.yml), [`js.yml`](.github/workflows/js.yml), [`wasm.yml`](.github/workflows/wasm.yml), [`android.yml`](.github/workflows/android.yml), [`android-native.yml`](.github/workflows/android-native.yml), [`windows.yml`](.github/workflows/windows.yml), [`linux.yml`](.github/workflows/linux.yml), [`macos.yml`](.github/workflows/macos.yml), [`ios.yml`](.github/workflows/ios.yml), [`tvos.yml`](.github/workflows/tvos.yml), [`watchos.yml`](.github/workflows/watchos.yml), [`swift.yml`](.github/workflows/swift.yml)) run only the jobs and runners that match a crate's real OS/FFI surface:

- **Cross-platform FFI (`libc-kotlin`):** run `jvm.yml` and `js.yml` across all 4 desktop hosts (`macosArm64`, `linuxX64`, `linuxArm64`, `mingwX64`) so each OS compiles and tests its real JNI / `.node` library.
- **OS-restricted FFI (`winapi-kotlin`, `windows-sys-kotlin`):** pass `platform: mingwX64` to `jvm.yml`, `js.yml`, and `windows.yml` (on `windows-latest`) instead of executing `#else` stubs on macOS or Linux.
- **Single-runner Wasm crates:** pure-Wasm computation crates only need one Wasm runner in CI (e.g. `macosArm64` or `linuxX64`), whereas a `wasmJs` Node N-API bridge (if kept) must run on the target OS runner whose `.node` binary it loads.
- Note [`publish.yml`](.github/workflows/publish.yml) when `project.target.android=false`: inspect whether `android-device-test` / `setupAndroidSdk` steps should be omitted in OS-restricted repos until `publish.yml` checks target opt-outs.

### 6.2 Template Plumbing Behavior & Remaining Notes

- **Host-scoped `verifyJniDistribution`:** [`FfiBuildSupport.resolveRequiredJniPayloads`](buildSrc/src/main/java/io/github/kotlinmania/build/FfiBuildSupport.java#L465-L510) automatically derives required JNI payloads from enabled desktop native targets (`macosArm64` → `macos-aarch64`, `linuxX64` → `linux-x86_64`, `linuxArm64` → `linux-aarch64`, `mingwX64` → `windows-x86_64`), or accepts an explicit `project.ffi.jvm.hosts` override.
- **Decoupled `compileAndroidJni`:** [`build.gradle.kts`](build.gradle.kts#L1398-L1410) registers `compileAndroidJni` under `if (jvmFfiEnabled && androidTargetEnabled)` independently of `jvmTargetEnabled`, and [`CentralPortalSupport.java`](buildSrc/src/main/java/io/github/kotlinmania/build/CentralPortalSupport.java#L131-L133) guards `verifyJniDistribution` when `jvm` is opted out.
- **Configurable Windows JNI link libraries:** [`FfiBuildSupport.resolveWindowsJniLinkFlags`](buildSrc/src/main/java/io/github/kotlinmania/build/FfiBuildSupport.java#L512-L534) links `-lws2_32` plus any libraries listed in `project.ffi.jvm.windowsLibraries`.
- **C/C++ preprocessor macros across channels:** `NodeFfiBuilder` injects `DEBUG` vs. `NDEBUG` ([`NodeFfiBuilder.java:426-431`](buildSrc/src/main/java/io/github/kotlinmania/build/NodeFfiBuilder.java#L426-L431)) and evaluates `binding.gyp` `OS` (`mac`/`linux`/`win`) and `target_arch` (`x64`/`arm64`) conditions ([`NodeFfiBuilder.java:88-94`](buildSrc/src/main/java/io/github/kotlinmania/build/NodeFfiBuilder.java#L88-L94)), whereas `FfiBuildSupport` passes `-DNDEBUG` in Release and `-g` in Debug (without `-DDEBUG`) and relies on compiler-predefined OS/ABI macros (`_WIN32`, `__APPLE__`, `__linux__`, `__ANDROID__`, `__aarch64__`, `__x86_64__`).
- **Host-local N-API build path:** `NodeFfiBuilder` writes `<addon>/build/<Debug|Release>/<product>.node` without an `<os>-<arch>` subdirectory ([`NodeFfiBuilder.java`](buildSrc/src/main/java/io/github/kotlinmania/build/NodeFfiBuilder.java#L413)), unlike `package<Name>Jni`'s `<ns>/native/<os>-<arch>/` layout.
