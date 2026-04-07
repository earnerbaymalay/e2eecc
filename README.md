<div align="center">

# 🛡️ E2EECC — Cypherchat
### *Your Messages Belong to You. Nobody Else.*

[![Status](https://img.shields.io/badge/Status-Alpha-50fa7b?style=for-the-badge)]()
[![Platform](https://img.shields.io/badge/Platform-Android_24%2B-4c566a?style=for-the-badge&logo=android)]()
[![PWA](https://img.shields.io/badge/PWA-iPhone_%7C_Any_Browser-81a1c1?style=for-the-badge&logo=apple)]()
[![Language](https://img.shields.io/badge/Kotlin-100%25-7F52FF?style=for-the-badge&logo=kotlin)]()
[![License](https://img.shields.io/badge/License-MIT-f1fa8c?style=for-the-badge)]()
[![Encryption](https://img.shields.io/badge/Encryption-AES--256--GCM+%2B+Double_Ratchet-bd93f9?style=for-the-badge)]()

[**⚡ Quick Start**](#-build--run-in-60-seconds) • [**📖 Usage Guide**](docs/USAGE.md) • [**🏗️ Architecture**](docs/ARCHITECTURE.md) • [**🔒 Security**](docs/SECURITY.md) • [**🗺️ Roadmap**](docs/ROADMAP.md) • [**🍎 iPhone PWA**](https://github.com/earnerbaymalay/e2eecc-pwa)

---

### 🤔 What is this?

Cypherchat is an **end-to-end encrypted messaging app** for Android that takes zero-knowledge seriously. No phone numbers. No usernames. No servers storing your data. **Nothing to subpoena, nothing to hack, nothing to sell.**

It uses the same cryptographic architecture as Signal — the **Double Ratchet** protocol — combined with **SimpleX** for identity-free communication. You don't create an account. You generate a keypair.

> **Most "encrypted" messengers still know who you talk to, when, and how often. Cypherchat doesn't even know you exist.**

### 🔥 Why does this exist?

| Your data on... | **WhatsApp** | **Telegram** | **Signal** | **Cypherchat** |
|---|---|---|---|---|
| 💬 Message content | ✅ Encrypted | ❌ Not by default | ✅ Encrypted | ✅ Encrypted |
| 📇 Contact graph | ❌ Stored | ❌ Stored | ⚠️ Partial | ✅ None |
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

### 👋 Who should use this?

> **If you believe your messages shouldn't be someone else's product, this is for you.**

| If you are... | Cypherchat gives you |
|---|---|
| 🟢 **Privacy-conscious user** | A messenger that actually respects your privacy. No phone number needed. |
| 🟡 **Journalist / activist** | Hardware-backed encryption with zero metadata. Invitations via secure links only. |
| 🔴 **Security researcher** | A real Double Ratchet + SimpleX implementation to study, audit, and extend. |
| 💻 **Developer** | Clean multi-module Kotlin architecture. Compose UI. Proper crypto separation. |

---

</div>

## 🚀 Build & Run in 60 Seconds

**Prerequisites:** Android Studio (Arctic Fox+), JDK 17, Android SDK 24-34.

```bash
# 1. Clone
git clone https://github.com/earnerbaymalay/e2eecc.git
cd e2eecc

# 2. Open in Android Studio
# File → Open → select the e2eecc directory

# 3. Sync Gradle (automatic on open)
# Wait for dependencies to resolve

# 4. Run on device or emulator
# Min SDK: 24 | Target SDK: 34 | Java: 17
```

**That's it.** The app launches with an onboarding screen → chat list → conversation view.

> ⚠️ **Alpha status:** The UI is fully built and navigable, but the crypto pipeline is a working skeleton — encryption/decryption functions correctly, but the network transport layer (SimpleX) needs a real SDK integration to send messages between devices. See [Roadmap](docs/ROADMAP.md).

---

## 🧠 What's Under the Hood

### Multi-Module Architecture

```
e2eecc/
├── app/                          ← Jetpack Compose UI + Koin DI
│   ├── CypherchatApplication     ← Application class, DI modules
│   ├── MainNavigation            ← Compose Nav (Onboarding → Chats → Conversation)
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

### Cryptographic Stack

| Layer | Algorithm | Purpose |
|---|---|---|
| **Key Storage** | Android Keystore (hardware-backed) | AES-256-GCM key generation & protection |
| **Symmetric Encryption** | AES-256-GCM | Message payload encryption with 128-bit auth tag |
| **Key Derivation** | HKDF-SHA256 (RFC 5869) | Root chain → chain key → message key derivation |
| **Key Agreement** | ECDH (P-256 / secp256r1) | Diffie-Hellman for Double Ratchet |
| **Double Ratchet** | Root chain + Message chains | Forward secrecy + break-in recovery |
| **Database at Rest** | SQLCipher (AES-256-CBC) | Full database file encryption |

### Security Guarantees

- ✅ **Forward secrecy** — each message uses a unique key; compromising one key doesn't reveal past messages
- ✅ **Break-in recovery** — DH ratchet step re-introduces randomness from peer's fresh key
- ✅ **Memory hygiene** — plaintext, chain keys, and passphrases are zeroed after use
- ✅ **Authenticated encryption** — GCM mode provides both confidentiality and integrity
- ✅ **Hardware-backed keys** — Android Keystore TEE prevents key extraction
- ✅ **Zero cleartext on disk** — only ciphertext is ever written to the database
- ✅ **Versioned envelope format** — future-proof wire protocol with AAD binding

---

## 🎨 UI Preview

The app uses a **dark-only theme** inspired by security tools:

| Screen | Description |
|---|---|
| **Onboarding** | Animated intro with feature list, "Get Started" CTA, privacy notice |
| **Chat List** | Conversation rows with avatars, last message preview, verification badges, unread counts |
| **Conversation** | Message bubbles (outgoing teal / incoming dark), E2EE notice banner, compose bar with send button |

See [design/cypherchat-ui.html](design/cypherchat-ui.html) for a visual reference.

---

## 🗺️ Roadmap

| Phase | Status | What's Done |
|---|---|---|
| **Phase 1: Crypto Foundation** | ✅ Complete | AES-256-GCM, HKDF, Double Ratchet, Keystore, SQLCipher DB |
| **Phase 2: UI Foundation** | ✅ Complete | Compose screens, navigation, theme, Koin DI |
| **Phase 3: Network Integration** | 🔄 In Progress | SimpleX transport interface defined, needs SDK integration |
| **Phase 4: Full Messaging** | 🔮 Planned | ViewModels, real-time message flow, invitation system |
| **Phase 5: Hardening** | 🔮 Planned | Security audit, key verification UI, proguard optimization |

See [docs/ROADMAP.md](docs/ROADMAP.md) for details.

---

## 📚 Documentation

| Document | What It Covers |
|---|---|
| [**Usage Guide**](docs/USAGE.md) | Start-to-finish: setup, first conversation, key verification, troubleshooting |
| [**Architecture**](docs/ARCHITECTURE.md) | Module design, data flow, dependency graph, architectural decisions |
| [**Security Overview**](docs/SECURITY.md) | Threat model, cryptographic protocols, key management, attack surface |
| [**Roadmap**](docs/ROADMAP.md) | Development phases, what's done, what's next |
| [**Contributing**](CONTRIBUTING.md) | How to contribute, coding standards, PR process |
| [**Security Policy**](SECURITY.md) | How to responsibly disclose vulnerabilities |

---

## 🍎 Also Available: Cypherchat PWA (iPhone)

**Got an iPhone?** Install Cypherchat directly from Safari — no App Store needed. Same zero-knowledge, end-to-end encrypted messaging, running entirely on your device.

👉 **[Get Cypherchat PWA →](https://github.com/earnerbaymalay/e2eecc-pwa)**

| | 📱 Android (this repo) | 🍎 iPhone (PWA) |
|---|---|---|
| **Crypto** | Android Keystore + JCA | WebCrypto API (same algorithms) |
| **Database** | Room + SQLCipher | IndexedDB (encrypted content) |
| **Install** | APK / F-Droid | Safari → Add to Home Screen |
| **Price** | Free | Free |
| **Privacy** | 100% local | 100% local |

> **Shared wire format:** Both apps use identical AES-256-GCM envelopes `[VERSION(1)][IV(12)][CT+TAG]`. Future: cross-platform message exchange via JSON export.

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
