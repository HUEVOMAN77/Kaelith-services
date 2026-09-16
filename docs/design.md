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

## 3. Module Hierarchy & Architecture

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
├── hcs-compat-db      # Community compatibility database client (schema & client interface)
├── hcs-update         # Self-updater (F-Droid style with signature validation)
├── hcs-telemetry      # Opt-in ACRA-style crash reporter (no Google services)
├── hcs-emui           # Non-privileged EMUI adapters (battery, autostart, launch manager)
├── hcs-privileged     # Optional root/ROM rollback & system patch manager
├── hcs-companion      # Companion GUI application & self-check UI dashboard
└── hcs-test-suite     # Instrumented and unit test suite
```

---

## 4. Phase 3 & 4 Architecture & Implementation Details

### 4.1 Phase 3: Auth & FIDO (`hcs-auth` & `hcs-fido`)
- **`hcs-auth`**: Open standards-based authentication client (`HcsAuthClient`). Wraps OAuth2 / OpenID Connect authorization flows without storing user credentials or secrets locally on the device.
- **`hcs-fido`**: Integrates Android `Credential Manager` and FIDO2 / WebAuthn options (`HcsFidoClient`). Facilitates passkey and biometric authentication for third-party apps without GMS FIDO dependencies.

### 4.2 Phase 4: Maps & WebView (`hcs-maps` & `hcs-webview`)
- **`hcs-maps`**: Modular map rendering provider abstraction (`HcsMapProvider`). Allows end-users and apps to switch between free open-source map backends (**MapLibre**, **OpenStreetMap**, **Mapbox**, or **VTM**) without relying on proprietary Google Maps v2 SDKs.
- **`hcs-webview`**: System WebView inspector (`HcsWebViewInspector`). Evaluates installed WebView package versions, multi-process capability, and Chromium engine rendering features on EMUI.

---

## 5. Non-Implementable APIs & Limitations (Honest Reporting)

The following Google services are explicitly marked as **Unimplementable (`Level D`)** in standard unprivileged mode:
1. **Google Play Integrity API & SafetyNet Attestation**: Requires Google server-side hardware attestation. Cannot be spoofed without violating safety & non-falsification rules.
2. **Google Play In-App Billing & Subscriptions**: Requires Play Store client and Google Pay backend.
3. **Google Wallet / Google Pay**: Requires proprietary Knox/TEE/SE secure element credentials and Google tokenization servers.
4. **Widevine L1 DRM (Custom Keys)**: Fallback to Widevine L3 if supported by hardware; proprietary DRM keys cannot be forged.
5. **Google Account Authentication via Proprietary Play Services**: Wrapped using standard OAuth2/OpenID where available, but proprietary GMS auth tokens cannot be synthesized.

---

## 6. Schemas for Future Modules

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
