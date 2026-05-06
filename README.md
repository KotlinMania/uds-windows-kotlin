# uds-windows-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fuds--windows--kotlin-blue.svg)](https://github.com/KotlinMania/uds-windows-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/uds-windows-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/uds-windows-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/uds-windows-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/uds-windows-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`haraldh/rust_uds_windows`](https://github.com/haraldh/rust_uds_windows).

**Original Project:** This port is based on [`haraldh/rust_uds_windows`](https://github.com/haraldh/rust_uds_windows). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `haraldh/rust_uds_windows`

> The text below is reproduced and lightly edited from [`https://github.com/haraldh/rust_uds_windows`](https://github.com/haraldh/rust_uds_windows). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## uds_windows

Forked from https://github.com/Azure/mio-uds-windows

A library for integrating Unix Domain Sockets on Windows. Similar to
the standard library's [support for Unix sockets][std].

## Structure

Most of the exported types in `uds_windows` are analagous to the
Unix-specific types in [std], but have been adapted for Windows.

Two "extension" traits, `UnixListenerExt` and `UnixStreamExt`, and their
implementations, were adapted from their TCP counterparts in the [miow] library.

## Windows support for Unix domain sockets
Support for Unix domain sockets was introduced in Windows 10
[Insider Build 17063][af-unix-preview]. It became generally available in version
1809 (aka the October 2018 Update), and in Windows Server 1809/2019.

[af-unix-preview]: https://blogs.msdn.microsoft.com/commandline/2017/12/19/af_unix-comes-to-windows
[mio]: https://github.com/carllerche/mio
[std]: https://doc.rust-lang.org/std/os/unix/net/
[miow]: https://github.com/alexcrichton/miow

# License

This project is licensed under MIT license ([LICENSE-MIT](https://github.com/haraldh/rust_uds_windows/blob/HEAD/LICENSE-MIT) or
http://opensource.org/licenses/MIT).

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:uds-windows-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`haraldh/rust_uds_windows`](https://github.com/haraldh/rust_uds_windows). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the rust_uds_windows authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`haraldh/rust_uds_windows`](https://github.com/haraldh/rust_uds_windows) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
