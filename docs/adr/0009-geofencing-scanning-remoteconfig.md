# 9. Extended Map Contracts, Geofencing, Barcode Scanning, and Remote Config

* Status: Accepted
* Date: 2026-03-06

## Context
Following the contract review against `common-mobile-services`, several API gaps were identified in map shapes/camera controls, geofencing, QR code scanning, and remote app configuration.

## Decision
1. **Enhanced Map Constructs (`hcs-maps`)**:
   - Enrich `hcs-maps` with `LatLng`, `MarkerOptions`, `PolylineOptions`, `PolygonOptions`, `CircleOptions`, and `CameraPosition` structs to align with standard mobile map APIs.
2. **Local Geofencing (`hcs-location`)**:
   - Implement `HcsGeofence` and `HcsGeofenceManager` using Android's native `LocationManager` proximity triggers.
3. **Open Barcode & QR Scanner (`hcs-scan`)**:
   - Provide an open, unprivileged `HcsBarcodeScanner` interface for 1D/2D QR and barcode decoding without proprietary MLKit SDKs.
4. **Open Remote Config (`hcs-remoteconfig`)**:
   - Provide `HcsRemoteConfigClient` for managing app parameters via local JSON or open REST backends.

## Consequences
- Near 1:1 public contract alignment with standard mobile service libraries.
- Zero dependency on proprietary GMS/HMS SDKs.
