# Cypherchat (e2eecc)

End-to-end encrypted messaging. No phone number, no username, and no account required.

[![Status](https://img.shields.io/badge/Status-Alpha-50fa7b?style=for-the-badge)]()
[![Platform](https://img.shields.io/badge/Platform-Android_24%2B-4c566a?style=for-the-badge&logo=android)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin)]()
[![Encryption](https://img.shields.io/badge/Encryption-AES--256--GCM+%2B+Double_Ratchet-bd93f9?style=for-the-badge)](docs/SECURITY.md)
[![License](https://img.shields.io/badge/license-MIT-f1fa8c?style=for-the-badge)](LICENSE)

[Quick start](#build-and-run) · [Usage guide](docs/USAGE.md) · [Architecture](docs/ARCHITECTURE.md) · [Security](docs/SECURITY.md) · [Roadmap](docs/ROADMAP.md)

---

## Functions

Cypherchat encrypts every message using the Double Ratchet protocol and AES-256-GCM. Instead of creating an account, you generate a unique keypair. Messages are encrypted on your device before transmission, ensuring the relay server only handles ciphertext and has no knowledge of sender or recipient identities.

---

## Cryptography

| Layer | Algorithm | Purpose |
|-------|-----------|---------|
| Key storage | Android Keystore (hardware-backed) | Generates and protects AES-256-GCM keys. |
| Symmetric encryption | AES-256-GCM | Encrypts message payloads with a 128-bit authentication tag. |
| Key derivation | HKDF-SHA256 (RFC 5869) | Derives message keys from the root chain. |
| Key agreement | ECDH (P-256 / secp256r1) | Performs Diffie-Hellman key exchange for the Double Ratchet. |
| Double Ratchet | Root chain + Message chains | Provides forward secrecy and break-in recovery. |
| Database at rest | SQLCipher (AES-256-CBC) | Encrypts the entire database file. |

Plaintext messages are never written to disk; only ciphertext is stored. Chain keys and passphrases are securely erased from memory after use.

---

## Build and run

Requires Android Studio Arctic Fox or later, JDK 17, and Android SDK 24-34.

```bash
git clone https://github.com/earnerbaymalay/e2eecc.git
cd e2eecc
```

Open the project in Android Studio, sync Gradle, and run it on a device or emulator (Minimum SDK 24, Target SDK 34).

The application starts with an onboarding sequence, followed by the chat list, and then the conversation view. Encryption and decryption are fully functional. The SimpleX transport layer needs a full SDK integration to enable message exchange between devices (refer to the [roadmap](docs/ROADMAP.md) for details).

---

## Architecture

```
e2eecc/
├── app/                          # Jetpack Compose UI, Koin DI
│   ├── CypherchatApplication
│   ├── MainActivity              # NavHost, PIN lock, JSON export
│   └── ui/screen/
│       ├── OnboardingScreen
│       ├── ChatListScreen
│       └── ConversationScreen
├── core/
│   ├── common/                   # SecureResult<T>, Logger, DispatcherProvider
│   ├── crypto/                   # KeyStoreManager, AesGcmCipher, HkdfDerivation, DoubleRatchetState
│   ├── database/                 # SQLCipher Room database
│   └── network/                  # SimplexTransport interface
└── design/cypherchat-ui.html     # UI reference mockup
```

---

## Roadmap

| Phase | Status | Objective |
|-------|--------|-----------|
| Crypto foundation | Done | AES-256-GCM, HKDF, Double Ratchet, Keystore, SQLCipher. |
| UI foundation | Done | Compose screens, navigation, Koin DI. |
| Core messaging | In progress | ViewModels are wired, crypto functions correctly, awaiting SimpleX SDK. |
| Advanced features | Planned | Media support, backup/restore, message retention. |
| Security hardening | Planned | Third-party audit, integration tests. |
| Distribution | Planned | Reproducible builds, F-Droid submission. |

Refer to [docs/ROADMAP.md](docs/ROADMAP.md) for further details.

---

## Related projects

- [iPhone PWA](https://earnerbaymalay.github.io/sideload/cypherchat/) - Install from Safari, WebCrypto encryption.
- [Sideload Hub](https://earnerbaymalay.github.io/sideload/) - Central hub for all applications.

---

[MIT License](LICENSE)
