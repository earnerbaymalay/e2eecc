<div align="center">

# 🛡️ E2EECC — Cypherchat
### *Your Messages Belong to You. Nobody Else.*

<p align="center">
  <img src="https://raw.githubusercontent.com/earnerbaymalay/sideload/main/assets/cypherchat-hero.svg" alt="Cypherchat Encryption" width="700"/>
</p>

[![Status](https://img.shields.io/badge/Status-Alpha-50fa7b?style=for-the-badge)](https://github.com/earnerbaymalay/e2eecc)
[![Platform](https://img.shields.io/badge/Platform-Android_24%2B-4c566a?style=for-the-badge&logo=android)](https://github.com/earnerbaymalay/e2eecc)
[![PWA](https://img.shields.io/badge/PWA-iPhone_%7C_Any_Browser-81a1c1?style=for-the-badge&logo=apple)](https://earnerbaymalay.github.io/sideload/cypherchat/)
[![Language](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-f1fa8c?style=for-the-badge)](LICENSE)
[![Encryption](https://img.shields.io/badge/Encryption-AES--256--GCM+%2B+Double_Ratchet-bd93f9?style=for-the-badge)](docs/SECURITY.md)

[**⚡ Quick Start**](#-build--run-in-60-seconds) · [**📖 Usage Guide**](docs/USAGE.md) · [**🏗️ Architecture**](docs/ARCHITECTURE.md) · [**🔒 Security**](docs/SECURITY.md) · [**🗺️ Roadmap**](docs/ROADMAP.md) · [**📲 iPhone PWA**](https://earnerbaymalay.github.io/sideload/cypherchat/)

</div>

---

## 🧬 The Mission

> *Picture this:* You need to send a sensitive message. You open a messaging app — but unlike WhatsApp, Telegram, or Signal, **this one doesn't even know you exist.** No phone number. No username. No server storing your contacts. The message is encrypted before it leaves your device, and the server it passes through has no idea who sent it or who receives it.

That's Cypherchat.

It uses the **same cryptographic architecture as Signal** — the Double Ratchet protocol with AES-256-GCM encryption — combined with **SimpleX** for identity-free communication. You don't create an account. You generate a keypair.

**Most "encrypted" messengers still know who you talk to, when, and how often. Cypherchat doesn't even know you exist.**

---

## 🔥 Why Does This Exist?

| Your messages on… | **WhatsApp** | **Telegram** | **Signal** | **Cypherchat** |
|---|---|---|---|---|
| 💬 Message content | ✅ Encrypted | ❌ Not by default | ✅ Encrypted | ✅ Encrypted |
| 📇 Contact graph | ❌ Stored | ❌ Stored | ⚠️ Partial | ✅ **None** |
| 🆔 User identity | Phone number | Phone number | Phone number | **None** |
| 🔑 Key storage | Server-assisted | Server | Server-assisted | **Device only** |
| 🏢 Company model | Meta (ads) | Pavel Durov | Non-profit | **Open source** |
| 💰 Revenue | Your data | Premium features | Donations | **Nothing to sell** |

Cypherchat goes further than any mainstream messenger by:
- 🚫 **Requiring zero identifiers** — no phone number, no email, no username
- 🔐 **Storing all keys in hardware** — Android Keystore (TEE/hardware-backed)
- 📭 **Leaving zero server footprint** — SimpleX relay has no idea who's talking
- 🧹 **Zero cleartext policy** — plaintext messages are never written to disk
- 🛡️ **Full at-rest encryption** — SQLCipher protects the database too

---

## 🔐 Cryptographic Stack

Every message is protected by multiple layers:

| Layer | Algorithm | Purpose |
|---|---|---|
| **Key Storage** | Android Keystore (hardware-backed) | AES-256-GCM key generation & protection |
| **Symmetric Encryption** | AES-256-GCM | Message payload encryption with 128-bit auth tag |
| **Key Derivation** | HKDF-SHA256 (RFC 5869) | Root chain → chain key → message key derivation |
| **Key Agreement** | ECDH (P-256 / secp256r1) | Diffie-Hellman for Double Ratchet |
| **Double Ratchet** | Root chain + Message chains | Forward secrecy + break-in recovery |
| **Database at Rest** | SQLCipher (AES-256-CBC) | Full database file encryption |

### Security Guarantees

- ✅ **Forward secrecy** — each message uses a unique key. Compromising one key reveals nothing about past messages.
- ✅ **Break-in recovery** — DH ratchet step re-introduces randomness from peer's fresh key.
- ✅ **Memory hygiene** — plaintext, chain keys, and passphrases are zeroed after use.
- ✅ **Authenticated encryption** — GCM mode provides both confidentiality and integrity.
- ✅ **Hardware-backed keys** — Android Keystore TEE prevents key extraction.
- ✅ **Zero cleartext on disk** — only ciphertext is ever written to the database.

---

## 👋 Who Should Use This?

| If you are… | Cypherchat gives you |
|---|---|
| 🟢 **Privacy-conscious user** | A messenger that actually respects your privacy. No phone number needed. |
| 🟡 **Journalist / activist** | Hardware-backed encryption with zero metadata. Invitations via secure links only. |
| 🔴 **Security researcher** | A real Double Ratchet + SimpleX implementation to study, audit, and extend. |
| 💻 **Android developer** | Clean multi-module Kotlin architecture. Compose UI. Proper crypto separation. |

---

## 🚀 Build & Run in 60 Seconds

**Prerequisites:** Android Studio (Arctic Fox+), JDK 17, Android SDK 24-34.

```bash
# 1. Clone
git clone https://github.com/earnerbaymalay/e2eecc.git
cd e2eecc

# 2. Open in Android Studio
# File → Open → select the e2eecc directory

# 3. Sync Gradle and run
# Min SDK: 24 | Target SDK: 34 | Java: 17
```

That's it. The app launches with an onboarding screen → chat list → conversation view.

> ⚠️ **Alpha status:** The UI is fully built and navigable, but the crypto pipeline is a working skeleton — encryption/decryption works correctly, but the network transport layer (SimpleX) needs a real SDK integration to send messages between devices. See [Roadmap](docs/ROADMAP.md).

---

## 🏗️ Architecture at a Glance

```
e2eecc/
├── app/                          ← Jetpack Compose UI + Koin DI
│   ├── CypherchatApplication     ← Application class, DI modules
│   ├── MainActivity              ← NavHost, PIN lock, JSON export
│   └── ui/
│       ├── theme/                ← Dark-only palette (CipherTeal accent)
│       └── screen/
│           ├── OnboardingScreen  ← Feature intro, "Get Started" CTA
│           ├── ChatListScreen    ← Conversation list with verification badges
│           └── ConversationScreen ← Message bubbles, E2EE notice, send bar
│
├── core/
│   ├── common/                   ← Shared utilities
│   │   ├── SecureResult<T>       ← Error-handling monad (Success/Failure)
│   │   ├── Logger                ← Debug-only logging (stripped in release)
│   │   └── DispatcherProvider    ← Coroutines thread abstraction
│   │
│   ├── crypto/                   ← 🔐 The encryption engine
│   │   ├── KeyStoreManager       ← Android Keystore AES-256-GCM key management
│   │   ├── AesGcmCipher          ← Envelope: [V(1)][IV(12)][CT+TAG]
│   │   ├── HkdfDerivation        ← RFC 5869 HKDF-SHA256 with memory zeroing
│   │   └── DoubleRatchetState    ← Signal-style ratchet (P-256 ECDH)
│   │
│   ├── database/                 ← 💾 At-rest encryption
│   │   ├── AppDatabase           ← SQLCipher-encrypted Room database
│   │   ├── entity/
│   │   │   ├── MessageEntity     ← Ciphertext-only storage (BLOBs)
│   │   │   └── ContactEntity     ← Peer identity records
│   │   └── dao/
│   │       ├── MessageDao        ← Flow-based message queries
│   │       └── ContactDao        ← Contact management
│   │
│   └── network/                  ← 🌐 Transport abstraction
│       ├── SimplexTransport      ← Interface for SimpleX relay protocol
│       └── MessageEnvelope       ← Wire format (binary serialization)
│
└── design/cypherchat-ui.html     ← UI reference mockup
```

### Data Flow

```
User types message
        │
        ▼
ConversationScreen (Compose UI)
        │
        ▼
ConversationViewModel
        │
        ├──► DoubleRatchetState.encryptMessage()  →  ciphertext
        │
        ├──► MessageDao.insert()  →  encrypted to SQLCipher DB
        │
        └──► SimplexTransport.send()  →  binary envelope to relay
```

---

## 📲 Available on Every Platform

Cypherchat is available as a **native Android app** (this repo) and a **Progressive Web App** for iPhone and any browser:

👉 **[Install Cypherchat PWA](https://earnerbaymalay.github.io/sideload/cypherchat/)**

| | 📱 Android (this repo) | 🍎 iPhone (PWA) |
|---|---|---|
| **Crypto** | Android Keystore + JCA | WebCrypto API (same algorithms) |
| **Database** | Room + SQLCipher | IndexedDB (encrypted content) |
| **Install** | APK / F-Droid | Safari → Add to Home Screen |
| **Price** | Free | Free |
| **Privacy** | 100% local | 100% local |

> **Shared wire format:** Both apps use identical AES-256-GCM envelopes `[VERSION(1)][IV(12)][CT+TAG]`. Cross-platform message exchange via JSON export/import.

### 📲 All Apps, One Place

All our apps are available through **[Sideload](https://earnerbaymalay.github.io/sideload/)** — our central distribution hub for local-first apps.

---

## 🗺️ Roadmap

| Phase | Status | What's Done |
|---|---|---|
| **Phase 1: Crypto Foundation** | ✅ Complete | AES-256-GCM, HKDF, Double Ratchet, Keystore, SQLCipher DB |
| **Phase 2: UI Foundation** | ✅ Complete | Compose screens, navigation, theme, Koin DI |
| **Phase 3: Core Messaging** | 🔄 In Progress | ViewModels wired, crypto works, needs SimpleX SDK |
| **Phase 4: Advanced Features** | 🔮 Planned | Media support, backup/restore, message retention |
| **Phase 5: Security Hardening** | 🔮 Planned | Third-party audit, penetration testing, integration tests |
| **Phase 6: Distribution** | 🔮 Planned | Reproducible builds, F-Droid submission, signed binaries |

See [docs/ROADMAP.md](docs/ROADMAP.md) for full details.

---

## 📚 Documentation

| Document | What It Covers |
|---|---|
| [**Usage Guide**](docs/USAGE.md) | Start-to-finish: setup, first conversation, PIN setup, export, troubleshooting |
| [**Architecture**](docs/ARCHITECTURE.md) | Module design, data flow, dependency graph, architectural decisions |
| [**Security Overview**](docs/SECURITY.md) | Threat model, cryptographic protocols, key management, attack surface |
| [**Roadmap**](docs/ROADMAP.md) | Development phases, what's done, what's next |
| [**Contributing**](CONTRIBUTING.md) | How to contribute, coding standards, PR process |
| [**Security Policy**](SECURITY.md) | How to responsibly disclose vulnerabilities |

---

## 🤝 Contributing

Cypherchat is built on the principle that **security software should be auditable**. Every line of code is open for inspection — and for improvement.

- 🐛 Found a bug? [Open an issue](https://github.com/earnerbaymalay/e2eecc/issues)
- 🔍 Security concern? See [SECURITY.md](SECURITY.md) for responsible disclosure
- 💡 Want to help? Read [CONTRIBUTING.md](CONTRIBUTING.md)
- 📖 Know crypto? Review the [DoubleRatchetState](core/crypto/src/main/java/com/cypherchat/core/crypto/DoubleRatchetState.kt) and suggest improvements

**Particularly welcome:**
- Cryptographers who want to review the Double Ratchet implementation
- Android developers who know Room + SQLCipher deeply
- SimpleX SDK integrators
- Security auditors

---

## 📜 License

[MIT License](LICENSE) — Use it. Audit it. Modify it. Share it.

**Security software should be open. Period.**

---

<div align="center">

### 🛡️ *No intermediaries. No metadata. No compromise.*

**⭐ If private messaging matters to you, star this repo — it helps more people discover it.**

</div>
