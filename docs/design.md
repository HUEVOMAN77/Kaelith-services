# HCS (Huawei Compatibility Services) - Design Document

## 1. Context & Objectives
**HCS (Huawei Compatibility Services)** is an open-source, modular compatibility layer for Android devices running Huawei EMUI / HarmonyOS without Google Play Services (GMS).
The main goal is to allow third-party applications (e.g. downloaded via Aurora Store) to run seamlessly without requiring GBox/GSpace sandboxes, proprietary GMS blobs, or unauthorized system modifications.

### Target Device Profile Reference
- **Model**: Huawei nova 10 (`NCO-LX3`)
- **SoC**: Qualcomm Snapdragon 778G 4G
- **OS**: Android 12 / EMUI 13.0.0.348 (`C605E2R1P2`)
- **Patch**: September 1, 2025
- **Architecture**: `arm64-v8a`

---

## 2. Architectural Principles & Non-Negotiable Rules

1. **Non-Falsification Policy**:
   - HCS will **never** fake or spoof Google signatures, Play Integrity/SafetyNet attestations, DRM keys, payment tokens, licenses, or accounts.
   - If an API cannot be provided legitimately or via open standards, HCS reports an honest incompatibility level (`Level D`).
2. **Three-Tier Operational Modes**:
   - **Mode 1: Normal Unprivileged App** (`hcs-companion` & `hcs-core`) - Zero root or system privileges required. Uses standard Android APIs & EMUI public settings intents.
   - **Mode 2: Optional Root Module** (`hcs-privileged`) - Explicit user opt-in, non-destructive, with full backup and 1-click rollback.
   - **Mode 3: ROM / AOSP Package** - Integrated at system build time for custom ROM developers.
3. **Privacy First & Zero Data Harvesting**:
   - No sensitive data collection (tokens, passwords, IMEI, phone numbers, Google accounts, or raw logs).
   - All exported logs and diagnostics are automatically redacted before saving or sharing.
   - Network telemetry and compatibility sharing are 100% opt-in.
4. **Trademark & Legal Integrity**:
   - Uses the public name **HCS** (Huawei Compatibility Services).
   - Pure clean-room implementation; zero proprietary Google/Huawei code.
5. **No Root / Play Integrity Evasion**:
   - HCS does not bypass root detection or mock bank security checks.

---

## 3. Complete Module Hierarchy & Architecture

```
hcs-core
├── hcs-api-compat     # Compatible interfaces for common Android / GMS calls
├── hcs-tasks          # Async tasks, callbacks, cancellation tokens (com.google.android.gms.tasks API compat)
├── hcs-location       # Fused location provider (Android Location / HMS / Free provider)
├── hcs-push           # UnifiedPush priority + FCM-compatible fallback + Huawei Push Kit
├── hcs-auth           # Credential & auth wrappers without stored secrets (OAuth2/OpenID)
├── hcs-maps           # Map provider interface (MapLibre / OpenStreetMap / Mapbox / VTM)
├── hcs-webview        # System WebView detection & alternative check
├── hcs-fido           # Credential Manager & WebAuthn wrappers
├── hcs-diagnostics    # System inspection, signature spoofing check, redacted logging
├── hcs-compat-db      # Community compatibility database client (anonymous read/report)
├── hcs-update         # Self-updater (F-Droid style with SHA-256 & signature validation)
├── hcs-telemetry      # Opt-in ACRA-style crash reporter without Google services
├── hcs-emui           # Non-privileged EMUI adapters (battery, autostart, launch manager)
├── hcs-privileged     # Optional root/ROM patch manager with backup, dry-run & 1-click rollback
├── hcs-companion      # Companion GUI application & self-check UI dashboard
└── hcs-test-suite     # Comprehensive instrumented and unit test suite
```

---

## 4. Full Phase Architecture & Implementation Details

### 4.1 Phase 1 & 2: Diagnostics, Tasks, Location & Push
- **`hcs-emui` & `hcs-diagnostics`**: Device profiling, EMUI battery/autostart launchers, signature spoofing status check, and PII-redacted logging.
- **`hcs-tasks`**: Clean-room implementation of `Task<T>`, `TaskCompletionSource<T>`, and `Tasks.await`.
- **`hcs-location`**: `FusedLocationProviderClient` wrapping GPS/Network and HMS Location Kit.
- **`hcs-push`**: Multi-transport push notification engine with **UnifiedPush** priority, FCM-compat adapter, and Huawei Push Kit fallback.

### 4.2 Phase 3 & 4: Auth, FIDO, Maps & WebView
- **`hcs-auth`**: Stateless OAuth2 / OpenID Connect authorization flows.
- **`hcs-fido`**: FIDO2 / WebAuthn passkey management via Android Credential Manager.
- **`hcs-maps`**: Abstracted `HcsMapProvider` supporting MapLibre, OpenStreetMap, Mapbox, and VTM.
- **`hcs-webview`**: System WebView rendering engine inspector.

### 4.3 Phase 5: Community DB, Updater & Telemetry (`hcs-compat-db`, `hcs-update`, `hcs-telemetry`)
- **`hcs-compat-db`**: Anonymous querying and opt-in submission of app compatibility reports.
- **`hcs-update`**: Cryptographically signed update index parser (`HcsUpdateManager`) verifying SHA-256 checksums and certificate fingerprints.
- **`hcs-telemetry`**: Opt-in crash reporter (`HcsCrashReporter`) capturing anonymized stack traces without Google services.

### 4.4 Phase 6: Privileged Module (`hcs-privileged`)
- **`hcs-privileged`**: `PrivilegedPatcher` providing safe system/root patch management with:
  - Pre-patch SHA-256 hash verification.
  - Full backup creation prior to any file modification.
  - Dry-run / simulation mode.
  - 1-click clean rollback and restoration.
  - Bootloop protection guard.

### 4.5 Phase 7: Companion GUI & Governance (`hcs-companion`, `CONTRIBUTING.md`, `SECURITY.md`)
- Complete unified dashboard UI featuring push status, map provider selection, app inspector with community DB lookup, update manager, privileged controls, and privacy panels.
- Comprehensive governance guidelines (`CONTRIBUTING.md` & `SECURITY.md`).

---

## 5. Non-Implementable APIs & Limitations (Honest Reporting)

The following Google services are explicitly marked as **Unimplementable (`Level D`)** in standard unprivileged mode:
1. **Google Play Integrity API & SafetyNet Attestation**: Requires Google server-side hardware attestation. Cannot be spoofed without violating safety & non-falsification rules.
2. **Google Play In-App Billing & Subscriptions**: Requires Play Store client and Google Pay backend.
3. **Google Wallet / Google Pay**: Requires proprietary Knox/TEE/SE secure element credentials and Google tokenization servers.
4. **Widevine L1 DRM (Custom Keys)**: Fallback to Widevine L3 if supported by hardware; proprietary DRM keys cannot be forged.
5. **Google Account Authentication via Proprietary Play Services**: Wrapped using standard OAuth2/OpenID where available, but proprietary GMS auth tokens cannot be synthesized.
