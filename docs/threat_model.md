# Threat Model

## Overview

This document formalizes the threats Cypherchat is designed to counter and the threats it acknowledges but does not (yet) counter.

## STRIDE Analysis

### S — Spoofing
**Threat:** An attacker impersonates a contact.
**Mitigation:** SimpleX invitation links include public keys. Key fingerprint verification UI (planned) will let users confirm identities.
**Risk:** MEDIUM (until key verification UI is built)

### T — Tampering
**Threat:** An attacker modifies messages in transit.
**Mitigation:** AES-256-GCM authentication tags detect any modification. Tampered messages fail decryption and are rejected.
**Risk:** LOW

### R — Repudiation
**Threat:** A sender denies having sent a message.
**Mitigation:** Not a goal — Cypherchat is a private messaging app, not a legally binding communication tool. The Double Ratchet protocol intentionally provides deniability.
**Risk:** N/A (by design)

### I — Information Disclosure
**Threat:** An attacker reads message content.
**Mitigation:** End-to-end AES-256-GCM encryption. Keys stored in Android Keystore (non-exportable). Database encrypted with SQLCipher.
**Risk:** LOW

### D — Denial of Service
**Threat:** An attacker floods the SimpleX relay with messages to a target queue.
**Mitigation:** SimpleX relay has rate limiting. Queue addresses are ephemeral and can be rotated.
**Risk:** MEDIUM (depends on relay implementation)

### E — Elevation of Privilege
**Threat:** A malicious app on the device gains access to Cypherchat's data.
**Mitigation:** Android app sandbox prevents cross-app data access. Keystore keys are app-scoped. SQLCipher database file is unreadable without the Keystore-derived passphrase.
**Risk:** LOW

## Data Flow Threats

### On the Wire (Network)
```
Device A ──[E2EE envelope]──► SimpleX Relay ──[E2EE envelope]──► Device B
```
- **Threat:** Relay operator reads or modifies messages
- **Mitigation:** Messages are encrypted before leaving Device A. Relay only sees ciphertext.

### At Rest (Device)
```
Plaintext ──[AES-256-GCM]──► Ciphertext ──[SQLCipher]──► Encrypted DB
                                      │
                            ┌─────────┴──────────┐
                            │   Android Keystore  │
                            │   (hardware-bound)  │
                            └─────────────────────┘
```
- **Threat:** Device theft or forensic analysis
- **Mitigation:** Keystore keys cannot be extracted. Database requires Keystore-derived passphrase.

### In Memory (RAM)
```
Plaintext ──► Encrypt ──► Ciphertext ──► [Plaintext zeroed]
```
- **Threat:** Memory scraping (malware, forensic tools)
- **Mitigation:** Plaintext is zeroed after encryption. Chain keys and passphrases are zeroed after derivation/use.
- **Gap:** JVM garbage collection may leave copies. Future: use `SecureMemory` or JNI-based allocation.

## Risk Matrix

| Threat | Likelihood | Impact | Risk Level | Status |
|---|---|---|---|---|
| Network MITM | High | Critical | 🔴 HIGH | ✅ Mitigated (E2EE) |
| Server compromise | Medium | High | 🟡 MEDIUM | ✅ Mitigated (SimpleX + E2EE) |
| Device theft | Medium | Critical | 🔴 HIGH | ✅ Mitigated (Keystore + SQLCipher) |
| Malware on device | Low | Critical | 🟡 MEDIUM | ⚠️ Partial (sandbox) |
| Key verification failure | Medium | High | 🟡 MEDIUM | 🔮 Planned (fingerprint UI) |
| Quantum attack | Low | Critical | 🟡 MEDIUM | 🔮 Research phase |
| Social engineering | Medium | High | 🟡 MEDIUM | ⚠️ User education needed |
| Supply chain attack | Low | Critical | 🟡 MEDIUM | ✅ Mitigated (pinned deps) |

## Assumptions

1. **Android OS is not compromised** — if the OS itself is malicious, all bets are off
2. **Android Keystore implementation is trustworthy** — we assume the TEE/StrongBox implementation is correct
3. **SimpleX relay is honest-but-curious** — the relay follows the protocol but may try to learn metadata
4. **Users verify key fingerprints** — until automated key verification is built, users should manually verify

## Future Threats

| Threat | Timeline | Mitigation Plan |
|---|---|---|
| Quantum computing | 10-20 years | Post-quantum key exchange (Kyber, NTRU) |
| AI-powered traffic analysis | 5-10 years | Cover traffic, timing obfuscation |
| Keystore vulnerabilities | Ongoing | Multiple key storage backends (Tink, BouncyCastle) |
| Regulatory pressure | Ongoing | No central server to subpoena, no user database |
