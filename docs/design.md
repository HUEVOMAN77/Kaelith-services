# HCS (Huawei Compatibility Services) - Design Document

## 1. Context & Objectives
**HCS (Huawei Compatibility Services)** is an open-source, modular compatibility layer for Android devices running Huawei EMUI / HarmonyOS without Google Play Services (GMS).
The main goal is to allow third-party applications to run seamlessly without requiring GBox/GSpace sandboxes, proprietary GMS blobs, or unauthorized system modifications.

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

## 3. Module Hierarchy & Architecture

```
hcs-core
├── hcs-api-compat     # Compatible interfaces for common Android / GMS calls
├── hcs-tasks          # Async tasks, callbacks, cancellation tokens
├── hcs-location       # Fused location provider (Android Location / HMS / Free provider)
├── hcs-push           # UnifiedPush priority + FCM-compatible fallback + Huawei Push Kit
├── hcs-auth           # Credential & auth wrappers without stored secrets
├── hcs-maps           # Map provider interface (MapLibre / OSM / Mapbox / VTM)
├── hcs-webview        # System WebView detection & alternative check
├── hcs-fido           # Credential Manager & WebAuthn wrappers
├── hcs-diagnostics    # System inspection, signature spoofing check, redacted logging
├── hcs-compat-db      # Community compatibility database client (schema & client interface)
├── hcs-update         # Self-updater (F-Droid style with signature validation)
├── hcs-telemetry      # Opt-in ACRA-style crash reporter (no Google services)
├── hcs-emui           # Non-privileged EMUI adapters (battery, autostart, launch manager)
├── hcs-privileged     # Optional root/ROM rollback & system patch manager
├── hcs-companion      # Companion GUI application & self-check UI dashboard
└── hcs-test-suite     # Instrumented and unit test suite
```

### Dependency Rules
- `hcs-core` modules must remain modular with **zero circular dependencies**.
- Higher-level modules (e.g. `hcs-companion`, `hcs-diagnostics`) depend on low-level abstractions (`hcs-api-compat`, `hcs-emui`).
- All external dependencies are tracked via Version Catalog (`gradle/libs.versions.toml`) with strict dependency locking for reproducible builds.

---

## 4. Phase 1 Implementation Scope

### 4.1 Device & EMUI Diagnostic (`hcs-emui` & `hcs-diagnostics`)
- **`EmuiCompatibilityProfile`**: Detects hardware model, SoC, Android version, EMUI version, HMS Core / Push Agent installation status, and device region (without PII).
- **EMUI Settings Launchers**: Safe intent callers to open EMUI-specific settings:
  - Battery Optimization (`com.huawei.systemmanager`)
  - App Auto-Start / Launch Manager (`com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity`)
  - Protected Apps / Power Genie settings.
  - Safe fallback to standard Android `Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` if EMUI activities are absent or inaccessible.

### 4.2 Signature Spoofing Verification
- Classifies Signature Spoofing capability into 5 distinct states:
  1. `SUPPORTED_AND_GRANTED`: Permitted and granted to HCS/GmsCore.
  2. `SUPPORTED_BUT_NOT_GRANTED`: System supports `fake-signature` permission, but user/ROM hasn't granted it.
  3. `UNSUPPORTED_BY_SYSTEM`: Android system/EMUI does not allow signature spoofing.
  4. `HUAWEISPECIFIC_RESTRICTION`: EMUI active integrity checks prevent spoofing.
  5. `UNKNOWN_ERROR`: Unable to query system permission state.
- **Strict Rule**: Unprivileged APK will never fake or report a false positive state.

### 4.3 App Inspector (`HcsAppInspector`)
Analyzes installed third-party APKs without decompiling or modifying them:
- Scans `AndroidManifest.xml` via `PackageManager` for:
  - Declared permissions, services, and receivers.
  - GMS libraries (`com.google.android.gms`, Firebase, Maps, FIDO, Billing, SafetyNet, Play Integrity).
  - HMS libraries (`com.huawei.hms`).
- Evaluates estimated compatibility level (A, B, C, D, E).
- Differentiates failure root causes:
  - `API_NOT_IMPLEMENTED`
  - `SIGNATURE_SPOOFING_MISSING`
  - `RESTRICTED_BY_EMUI_BATTERY`
  - `WEBVIEW_DEPENDENCY_MISSING`
  - `NETWORK_FAIL`

### 4.4 Compatibility Level Matrix

| Level | Meaning |
|---|---|
| **A** | Tested public API fully functional on the target device profile. |
| **B** | Operates with documented non-critical limitations. |
| **C** | Operates only under specific conditions or manual setup (e.g. manual battery exemption). |
| **D** | Unimplementable without proprietary Google binaries, root, or custom ROM. |
| **E** | Untested / Unknown. |

---

## 5. Non-Implementable APIs & Limitations (Honest Reporting)

The following Google services are explicitly marked as **Unimplementable (`Level D`)** in standard unprivileged mode:
1. **Google Play Integrity API & SafetyNet Attestation**: Requires Google server-side hardware attestation. Cannot be spoofed without violating safety & non-falsification rules.
2. **Google Play In-App Billing & Subscriptions**: Requires Play Store client and Google Pay backend.
3. **Google Wallet / Google Pay**: Requires proprietary Knox/TEE/SE secure element credentials and Google tokenization servers.
4. **Widevine L1 DRM (Custom Keys)**: Fallback to Widevine L3 if supported by hardware; proprietary DRM keys cannot be forged.
5. **Google Account Authentication via Proprietary Play Services**: Wrapped using standard OAuth2/OpenID where available, but proprietary GMS auth tokens cannot be synthesized.

---

## 6. Schemas for Future Modules (Phase 1 Outline)

### 6.1 `hcs-compat-db` Schema Outline
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "HcsCompatibilityReport",
  "type": "object",
  "properties": {
    "package_name": { "type": "string" },
    "app_version_code": { "type": "integer" },
    "app_version_name": { "type": "string" },
    "device_model": { "type": "string" },
    "emui_version": { "type": "string" },
    "android_sdk": { "type": "integer" },
    "compatibility_level": { "type": "string", "enum": ["A", "B", "C", "D", "E"] },
    "failing_apis": {
      "type": "array",
      "items": { "type": "string" }
    },
    "notes": { "type": "string" },
    "timestamp": { "type": "integer" }
  },
  "required": ["package_name", "compatibility_level", "device_model", "android_sdk"]
}
```

### 6.2 `hcs-update` Schema Outline
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "HcsUpdateIndex",
  "type": "object",
  "properties": {
    "version_code": { "type": "integer" },
    "version_name": { "type": "string" },
    "download_url": { "type": "string" },
    "sha256_checksum": { "type": "string" },
    "min_sdk": { "type": "integer" },
    "release_notes": { "type": "string" },
    "signature_fingerprint_sha256": { "type": "string" }
  },
  "required": ["version_code", "version_name", "download_url", "sha256_checksum", "signature_fingerprint_sha256"]
}
```

---

## 7. Verification & Acceptance Criteria (Phase 1)
- Clean, reproducible Gradle build setup (`./gradlew assembleDebug`).
- Complete `EmuiCompatibilityProfile` and `HcsAppInspector` unit tests.
- Standalone `hcs-companion` APK signed with test key.
- Safe, non-crashing EMUI settings launcher with fallbacks.
- ES/EN bilingual self-check dashboard UI.
