# 🔒 Security Overview & Threat Model

## Threat Model

### Assets We Protect
1. **Message content** — the actual text of communications
2. **Message metadata** — who you talk to, when, and how often
3. **Cryptographic keys** — the keys that unlock everything above
4. **Contact identities** — who you're communicating with

### Threat Actors

| Actor | Capability | What Cypherchat Prevents |
|---|---|---|---|
| **Network adversary** | Passive MITM, active MITM | ✅ Cannot read messages (E2EE with AES-256-GCM) |
| **Server operator** | Full server access | ✅ Cannot read messages (E2EE), cannot identify users (SimpleX) |
| **Device thief** | Physical access to phone | ✅ Cannot read messages (Keystore + SQLCipher) |
| **Forensic analyst** | Law enforcement with device | ✅ Cannot extract keys (Keystore non-exportable), cannot read DB (SQLCipher) |
| **Malicious app** | Other apps on the device | ✅ Cannot access Keystore keys (app-scoped), cannot read DB file |
| **Compromised contact** | Their device is hacked | ✅ Past messages safe (forward secrecy), future messages safe after DH ratchet |

### What Cypherchat Does NOT (Yet) Protect Against

| Threat | Status | Mitigation Plan |
|---|---|---|
| **Endpoint compromise** (malware on device) | ⚠️ Not protected | Out of scope — requires OS-level security |
| **Social engineering** | ⚠️ Not protected | Key verification UI (planned) will help |
| **Screen capture / keylogger** | ⚠️ Not protected | FLAG_SECURE in release builds (planned) |
| **Quantum computing** | 🔮 Future | Post-quantum key exchange (research phase) |

## Cryptographic Protocols

### 1. Symmetric Encryption: AES-256-GCM

**Purpose:** Encrypt individual message payloads

```
Envelope: [VERSION(1)] [IV(12)] [CIPHERTEXT + TAG(variable)]
```

| Property | Value |
|---|---|
| Algorithm | AES/GCM/NoPadding |
| Key size | 256 bits |
| IV size | 96 bits (random per operation) |
| Tag size | 128 bits |
| Mode | Authenticated encryption |

**Security properties:**
- **Confidentiality** — AES-256 ensures plaintext cannot be recovered without the key
- **Integrity** — GCM authentication tag detects any modification
- **Authenticity** — AAD binds the ciphertext to its context (message ID, sender fingerprint)
- **Nonce safety** — random 96-bit IV makes collision probability negligible (2^-48 after 2^32 messages)

### 2. Key Derivation: HKDF-SHA256

**Purpose:** Derive chain keys and message keys from root material

| Property | Value |
|---|---|
| Protocol | RFC 5869 |
| HMAC | SHA-256 |
| Output | 32 bytes (256 bits) |
| Memory hygiene | Input key material zeroed after derivation |

**Security properties:**
- **Pseudorandomness** — derived keys are indistinguishable from random
- **Independence** — each derived key is cryptographically independent
- **Zeroing** — source material is wiped to prevent memory scraping

### 3. Key Agreement: ECDH (P-256)

**Purpose:** Diffie-Hellman key exchange for Double Ratchet

| Property | Value |
|---|---|
| Curve | secp256r1 (P-256) |
| API | JCA KeyAgreement ("ECDH") |
| Output | Shared secret (shared point × coordinate) |

**Note:** X25519 (Curve25519) is preferred in Signal's implementation but Android Keystore has limited support. P-256 is used for broad API compatibility. X25519 support is planned for API 31+ with BouncyCastle fallback.

### 4. Double Ratchet Protocol

**Purpose:** Forward secrecy and break-in recovery for message keys

**Architecture:**
```
Root Key ──┬── DH output ──→ KDF_RK ──→ New Root Key + New Chain Key
           │
     Chain Key ──→ KDF_CK ──→ New Chain Key + Message Key
                                      │
                                      └── AES-256-GCM encrypt
```

**Security properties:**
- **Forward secrecy** — each message uses a unique key. Compromising the current state reveals nothing about past messages.
- **Break-in recovery** — the DH ratchet step introduces fresh entropy from the peer's new ratchet key. After one DH step, past compromises are irrelevant.
- **Out-of-order delivery** — skipped message keys are cached (max 1000) to handle network reordering.
- **Self-healing** — if one message is lost, the chains re-synchronize on the next DH ratchet step.

### 5. Key Storage: Android Keystore

**Purpose:** Hardware-backed key generation and storage

| Property | Value |
|---|---|
| Backend | AndroidKeyStore (TEE / StrongBox) |
| Key type | AES-256-GCM |
| Exportability | Non-exportable (hardware-bound) |
| Access control | App-scoped, user authentication (optional) |

**Security properties:**
- **Non-exportable** — keys cannot be extracted from the TEE, even with root access
- **App-scoped** — other apps cannot access Cypherchat's keys
- **Hardware-backed** — on devices with StrongBox, keys are in a dedicated secure element
- **Rate-limited** — failed authentication attempts are rate-limited by the TEE

### 6. Database at Rest: SQLCipher

**Purpose:** Encrypt the entire Room database file

| Property | Value |
|---|---|
| Algorithm | AES-256-CBC |
| Key derivation | PBKDF2 (iterations configured in SQLCipher) |
| Passphrase source | Android Keystore (device-bound) |
| Passphrase lifecycle | Zeroed immediately after database open |

**Security properties:**
- **Defense in depth** — even if Keystore is bypassed, the database file is independently encrypted
- **Full file encryption** — every byte of the database is encrypted, including indexes and metadata
- **Device binding** — the passphrase is derived from a Keystore key, making the database unreadable on another device

## Attack Surface Analysis

### External Attack Surface
- **SimpleX relay connection** — TLS to relay server (certificate pinning planned)
- **Invitation links** — contain queue addresses and public keys (no secret material)

### Internal Attack Surface
- **Memory** — plaintext exists briefly in RAM during encrypt/decrypt. Mitigated by zeroing.
- **Logs** — `Logger` is debug-only, stripped in release builds
- **Backups** — Android backup rules exclude app data (see `backup_rules.xml`)

### Supply Chain Risk
- **Dependencies** — all dependencies are pinned in `libs.versions.toml`
- **Build integrity** — Gradle wrapper checksums verified
- **CI/CD** — GitLab CI runs security scanning (detect-secrets, OWASP DependencyCheck)

## Security Checklist

- [x] AES-256-GCM for message encryption
- [x] Random IV per operation (no reuse)
- [x] 128-bit authentication tag
- [x] HKDF-SHA256 for key derivation
- [x] Double Ratchet for forward secrecy
- [x] Android Keystore for key storage
- [x] SQLCipher for database encryption
- [x] Plaintext zeroing after use
- [x] Passphrase zeroing after DB open
- [x] Debug logging disabled in release builds
- [x] ProGuard minification enabled for release
- [x] Android backup excluded
- [ ] Certificate pinning for SimpleX relay
- [ ] FLAG_SECURE to prevent screenshots (release builds)
- [ ] Key verification UI (fingerprint comparison)
- [ ] X3DH handshake protocol
- [ ] Post-quantum key exchange research

## Comparison to Signal

| Property | Signal | Cypherchat |
|---|---|---|
| Message encryption | AES-256-GCM + Double Ratchet | AES-256-GCM + Double Ratchet |
| Key storage | Software + Keystore | Android Keystore only |
| User identity | Phone number | None (SimpleX) |
| Server metadata | Knows who talks to whom | None (identity-free) |
| Database encryption | SQLCipher | SQLCipher |
| Forward secrecy | ✅ | ✅ |
| Open source | ✅ | ✅ |
| Maturity | Production (10+ years) | Alpha |

---

*This document is a living description of Cypherchat's security posture. It should be updated with every significant cryptographic change.*
