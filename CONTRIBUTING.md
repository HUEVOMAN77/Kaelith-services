# Contributing to HCS (Huawei Compatibility Services)

Thank you for your interest in contributing to **HCS**!

## Guidelines & Principles
1. **Clean-Room Implementation**: Never copy proprietary Google or Huawei code/SDKs.
2. **Honest Compatibility**: Never fake Google Play Integrity, SafetyNet attestations, or Play Billing signatures.
3. **Privacy First**: Always redact PII (emails, tokens, IMEIs, phone numbers) before logging or submitting telemetry.
4. **Code Quality**: Ensure all code passes `./gradlew check test` before submitting pull requests.

## Workflow
1. Fork the repository.
2. Create a descriptive feature branch (`feature/my-feature`).
3. Write modular code and accompanying unit tests in `hcs-test-suite` or module test directories.
4. Verify build and tests locally using `./gradlew check test`.
5. Open a Pull Request following the 10-point report format outlined in the documentation.
