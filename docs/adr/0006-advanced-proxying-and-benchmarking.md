# 6. Advanced Local Proxying and Performance Profiling

* Status: Accepted
* Date: 2026-03-06

## Context
Legacy apps downloaded from Aurora Store may hardcode calls to Google URLs (`android.googleapis.com`) or require seamless passkey biometrics and offline profile transfers.

## Decision
1. **Local Micro-Proxy (`hcs-proxy`)**: Handle legacy Google pings locally without external network stalls.
2. **Performance Benchmark (`hcs-benchmark`)**: Measure memory and battery overhead vs native GMS.
3. **Automated UnifiedPush Installer (`hcs-distributor-installer`)**: Manage ntfy/Gotify distributors seamlessly.
4. **EMUI Biometric FIDO (`hcs-fido-biometrics`)**: Link EMUI BiometricPrompt with FIDO2 passkeys.
5. **Offline Profiles (`hcs-offline-profiles`)**: Support `.hcsjson` profile import/export.

## Consequences
- Enhanced compatibility for legacy apps.
- Full transparency regarding battery efficiency and offline management.
