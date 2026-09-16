# 4. Open Standards Auth and Configurable Map Providers

* Status: Accepted
* Date: 2026-03-06

## Context
Apps downloaded from Aurora Store often call Google Sign-In or Google Maps v2 SDKs. On Huawei EMUI devices without GMS, these calls fail or crash.

## Decision
1. **Open Standards Auth (`hcs-auth` & `hcs-fido`)**:
   - Replace proprietary Google Auth with standard OAuth2 / OpenID Connect authorization code flows with PKCE.
   - Leverage Android `Credential Manager` and FIDO2 / WebAuthn for passkeys without storing secrets on the device.
2. **Configurable Map Engine (`hcs-maps`)**:
   - Abstracize map views behind an `HcsMapProvider` interface.
   - Support user-selected open-source rendering engines: **MapLibre**, **OpenStreetMap (osmdroid)**, **Mapbox**, or **VTM**.

## Consequences
- Applications can perform secure authentication without GMS auth blobs.
- Apps displaying interactive maps can run using open map tile providers without proprietary Google Maps SDKs.
