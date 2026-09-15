# 1. Architecture Overview and Modularization

* Status: Accepted
* Date: 2026-03-06

## Context
Applications originally built for Android devices with Google Play Services (GMS) often fail on Huawei devices running EMUI / HarmonyOS without GMS. To address this issue without relying on heavy sandboxes (e.g. GBox, GSpace) or modifying system partitions, a clean, modular compatibility service layer is needed.

## Decision
We establish **HCS (Huawei Compatibility Services)** as a modular project (`hcs-core`) composed of independent modules:
- `hcs-api-compat`: Public interface definitions for common Android / GMS APIs.
- `hcs-tasks`: Async task management (`Task`, `TaskCompletionSource`, callbacks).
- `hcs-location`: Location abstraction (Android Location, HMS Location, or open providers).
- `hcs-push`: Push messaging transport wrapper (UnifiedPush prioritized, FCM fallback, Huawei Push Kit).
- `hcs-auth`: Authentication wrappers without stored secrets.
- `hcs-maps`: Map provider abstraction (MapLibre / OSM / Mapbox / VTM).
- `hcs-webview`: System WebView capability detector and fallback checker.
- `hcs-fido`: FIDO2 and Credential Manager wrappers.
- `hcs-diagnostics`: System inspection, signature spoofing check, redacted logging.
- `hcs-compat-db`: Client for community compatibility database.
- `hcs-update`: F-Droid style self-updater with cryptographic signature validation.
- `hcs-telemetry`: Opt-in ACRA-style crash reporter without Google dependencies.
- `hcs-emui`: Non-privileged EMUI adapters (battery, autostart, power management).
- `hcs-privileged`: Optional root / AOSP ROM system module with 1-click rollback.
- `hcs-companion`: Companion GUI app with self-check dashboard and diagnostics.
- `hcs-test-suite`: Comprehensive unit and instrumented testing module.

## Consequences
- Clean separation of concerns with zero circular dependencies across `hcs-core`.
- High maintainability and testability for each compatibility module.
- Flexible deployment (unprivileged APK vs optional root module vs custom ROM package).
