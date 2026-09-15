# 2. Honest Compatibility and Non-Falsification Policy

* Status: Accepted
* Date: 2026-03-06

## Context
Certain Google APIs (such as Play Integrity API, SafetyNet, Google Play Billing, and Google Wallet) rely on Google server-backed attestation and hardware-backed keys. Simulating or spoofing these attestations violates safety standards, risks security bans, and creates false expectations for users.

## Decision
HCS strictly enforces an **Honest Compatibility and Non-Falsification Policy**:
1. HCS will **never** fake certificates, Play Integrity tokens, DRM keys, payment credentials, or Google account signatures.
2. If an API requires proprietary Google infrastructure that cannot be provided legitimately, HCS will explicitly classify it as **Unimplementable (`Level D`)**.
3. Diagnostic tools (`HcsAppInspector`) will explicitly identify when an app fails due to an unimplementable Google proprietary dependency rather than masking the error.

## Consequences
- Transparent compatibility matrix for users and developers.
- Protection against security risks and software bans.
- Users know exactly which apps are fully functional (Level A/B) and which require Google hardware attestation (Level D).
