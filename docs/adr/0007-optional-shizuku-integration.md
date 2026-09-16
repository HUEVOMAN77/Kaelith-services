# 7. Optional Shizuku Shell Helper Integration

* Status: Accepted
* Date: 2026-03-06

## Context
Shizuku is an open-source tool that exposes `adb shell` level privileges to authorized Android applications without requiring root access or bootloader unlocking.

## Decision
1. **Optional Additive Integration (`hcs-shizuku`)**:
   - Introduce `hcs-shizuku` as a standalone helper module.
   - Core HCS functionality must work seamlessly without Shizuku.
   - If Shizuku is installed and authorized by the user, HCS gains enhanced `dumpsys` battery status inspection, battery whitelist management (`dumpsys deviceidle whitelist`), and expanded package inspection.
2. **Explicit Scope Boundaries**:
   - Shizuku will **never** be used to install Google Play Services, forge signatures, or bypass Play Integrity/SafetyNet.
   - Two-level user consent is strictly enforced: user installs Shizuku manually and explicitly authorizes HCS.

## Consequences
- Enhanced diagnostic precision for users running Shizuku.
- Zero regression for standard unprivileged users without Shizuku.
