# 8. Optional GMS Package Identity Bridge for Root/Privileged Mode

* Status: Accepted
* Date: 2026-03-06

## Context
Applications like YouTube or apps using legacy Google Maps SDKs hardcode checks looking for the package `com.google.android.gms`. Without package identity spoofing, these apps refuse to launch or crash on GMS-free Huawei EMUI devices, even if HCS provides the underlying location and async task capabilities.

## Decision
1. **Optional GMS Bridge (`hcs-gms-bridge`)**:
   - Create a sub-module under `hcs-privileged` that exposes existing HCS core services (`hcs-tasks`, `hcs-location`, `hcs-push`, `hcs-auth`, `hcs-maps`, `hcs-fido`) under the package identity `com.google.android.gms`.
   - **Pre-Activation Environment Check**: Activate ONLY if signature spoofing capability is available in the user's root/ROM environment.
   - **Exclusivity & Collision Warning**: Check for conflicts with microG / GmsCore. Prompt user to uninstall microG before enabling HCS GMS Bridge.
   - **No Hardware Attestation Falsification**: GMS Bridge only satisfies local package identity lookups. Google Play Integrity, SafetyNet, and Google Pay remain unimplementable (`Level D`).

## Consequences
- YouTube basic video playback and search become functional (`Level B`).
- Zero code duplication by routing IPC calls directly to existing HCS runtime modules.
- Complete rollback and opt-in user control preserved.
