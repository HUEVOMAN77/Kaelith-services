# 3. Push Notification Architecture with UnifiedPush Priority

* Status: Accepted
* Date: 2026-03-06

## Context
Third-party Android applications downloaded from stores like Aurora Store rely heavily on push notifications (e.g. Firebase Cloud Messaging / FCM) to deliver messages and background events. On Huawei EMUI devices without GMS, apps that rely solely on proprietary FCM services fail to receive background push alerts.

## Decision
We establish a three-layer push notification architecture for HCS (`hcs-push`):
1. **UnifiedPush as Priority Transport**: UnifiedPush is an open, decentralized push protocol standard. HCS prioritizes UnifiedPush distributors (such as ntfy or embedded distributor) to provide zero-GMS, privacy-first push notifications.
2. **FCM-Compatible Receiver Adapter**: To maintain compatibility with third-party apps designed for GMS, `hcs-push` includes a broadcast receiver and service dispatching layer that intercepts and routes FCM/C2DM intents (`com.google.android.c2dm.intent.RECEIVE`).
3. **Huawei Push Kit Fallback**: For EMUI devices where Huawei Push Kit (`com.huawei.android.pushagent`) is present, HCS provides a fallback bridge to route push messages when UnifiedPush is unavailable.

## Consequences
- Applications downloaded from Aurora Store receive push notifications without GMS.
- Users enjoy zero vendor lock-in and lower battery consumption via UnifiedPush.
- Full diagnostic transparency allowing users to choose their preferred push transport.
