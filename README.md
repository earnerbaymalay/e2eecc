# Cypherchat (e2eecc)

End-to-end encrypted messaging. No phone number, no username, no account.

[![Status](https://img.shields.io/badge/Status-Alpha-50fa7b?style=for-the-badge)]()
[![Platform](https://img.shields.io/badge/Platform-Android_24%2B-4c566a?style=for-the-badge&logo=android)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin)]()
[![Encryption](https://img.shields.io/badge/Encryption-AES--256--GCM+%2B+Double_Ratchet-bd93f9?style=for-the-badge)](docs/SECURITY.md)
[![License](https://img.shields.io/badge/license-MIT-f1fa8c?style=for-the-badge)](LICENSE)

[Quick Start](#build-and-run) · [Usage Guide](docs/USAGE.md) · [Architecture](docs/ARCHITECTURE.md) · [Security](docs/SECURITY.md) · [Roadmap](docs/ROADMAP.md)

---

## What It Does

Cypherchat encrypts every message with the Double Ratchet protocol and AES-256-GCM. You do not create an account -- you generate a keypair. Messages are encrypted before they leave your device. The relay server sees only ciphertext and has no idea who sent or receives anything.

## Cryptography

| Layer | Algorithm | Purpose |
|-------|-----------|---------|
| Key storage | Android Keystore (hardware-backed) | AES-256-GCM key generation and protection |
| Symmetric encryption | AES-256-GCM | Message payload with 128-bit auth tag |
| Key derivation | HKDF-SHA256 (RFC 5869) | Root chain to message key derivation |
| Key agreement | ECDH (P-256 / secp256r1) | Diffie-Hellman for Double Ratchet |
| Double Ratchet | Root chain + Message chains | Forward secrecy and break-in recovery |
| Database at rest | SQLCipher (AES-256-CBC) | Full database file encryption |

Plaintext messages are never written to disk. Only ciphertext is stored. Chain keys and passphrases are zeroed from memory after use.

## Build and Run

Android Studio Arctic Fox or later, JDK 17, Android SDK 24-34.

```bash
git clone https://github.com/earnerbaymalay/e2eecc.git
cd e2eecc
```

Open in Android Studio, sync Gradle, and run. Min SDK 24, target SDK 34.

The app launches with onboarding, then chat list, then conversation view. Encryption and decryption work correctly. The SimpleX transport layer needs a real SDK integration to send messages between devices -- see the [roadmap](docs/ROADMAP.md).

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

## Roadmap

| Phase | Status | What |
|-------|--------|------|
| Crypto foundation | Done | AES-256-GCM, HKDF, Double Ratchet, Keystore, SQLCipher |
| UI foundation | Done | Compose screens, navigation, Koin DI |
| Core messaging | In progress | ViewModels wired, crypto works, needs SimpleX SDK |
| Advanced features | Planned | Media support, backup/restore, message retention |
| Security hardening | Planned | Third-party audit, integration tests |
| Distribution | Planned | Reproducible builds, F-Droid submission |

See [docs/ROADMAP.md](docs/ROADMAP.md) for details.

## Also Available

- [iPhone PWA](https://earnerbaymalay.github.io/sideload/cypherchat/) -- Install from Safari, WebCrypto encryption
- [Sideload Hub](https://earnerbaymalay.github.io/sideload/) -- All apps in one place

---

[MIT License](LICENSE)
