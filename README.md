# 🔐 Cypherchat

[![Source](https://img.shields.io/badge/source-github-blue?logo=github)](https://github.com/earnerbaymalay/e2eecc)
[![License](https://img.shields.io/badge/license-AGPL--3.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.en.html)
[![Status](https://img.shields.io/badge/status-alpha-orange)](https://github.com/earnerbaymalay/e2eecc)
[![Platform](https://img.shields.io/badge/platform-Android-green?logo=android)](https://www.android.com/)

---

## 🚀 Hook

**Stop trading your privacy for convenience.**

Imagine a messaging app where your phone number doesn't exist. Your email is irrelevant. Your real name is optional. Even the servers routing your messages have **zero idea** who you are, who you're talking to, or what you're saying.

**That's Cypherchat.**

The messaging app built for people who refuse to accept that privacy is dead. Where every conversation is protected by hardware-backed encryption. Where your identity stays yours. Where surveillance capitalism gets a hard "no."

**No accounts. No tracking. No backdoors. No compromises.**

Just you, your contacts, and conversations that actually stay between you—encrypted end-to-end, stored locally, and routed through a network that leaves zero digital fingerprints.

**Your conversations deserve better than being data-mined. Welcome to Cypherchat.**

---

## 📑 Table of Contents

- [🚀 Hook](#-hook)
- [💭 The 'Why': Our Philosophy](#-the-why-our-philosophy)
- [🚀 Getting Started](#-getting-started)
  - [For Users](#for-users)
  - [For Developers](#for-developers)
- [✨ Feature Deep Dive](#-feature-deep-dive)
  - [Identity-Free Messaging](#identity-free-messaging)
  - [Hardware-Backed Security](#hardware-backed-security)
  - [Zero-Knowledge Architecture](#zero-knowledge-architecture)
  - [Metadata Minimization](#metadata-minimization)
  - [Encrypted Local Storage](#encrypted-local-storage)
- [📚 Glossary](#-glossary)
- [🗺️ Dev Roadmap](#️-dev-roadmap)
- [🤝 Contributing](#-contributing)
- [📞 Support & Feedback](#-support--feedback)
- [📄 License](#-license)
- [🔗 Links](#-links)

---

## 💭 The 'Why': Our Philosophy

### Privacy is Not Optional

In a world where every message, every contact, every moment of your digital life is harvested, analyzed, and monetized, we believe you deserve a choice. Cypherchat exists because **privacy should be the default, not a premium add-on**.

We're not building another messaging app. We're building a statement: that your conversations belong to you, not to advertisers, governments, or data brokers.

### Trust Through Transparency

We don't ask you to trust us blindly. Every line of code is open source. Every security decision is documented. Every architectural choice is auditable. **You shouldn't have to trust us—you should be able to verify us.**

Our entire codebase lives on GitHub under AGPL-3.0. Security researchers can audit it. Cryptographers can review it. You can read it. Because when it comes to privacy, transparency isn't optional—it's essential.

### Security by Design, Not by Accident

Cypherchat isn't a messaging app with security bolted on. It's a security-first messaging platform built from the ground up with:

- **Hardware-backed keys** that never leave your device's secure enclave
- **Zero-knowledge architecture** where servers literally cannot see your messages
- **Identity-free transport** that breaks the link between you and your conversations
- **Memory-safe practices** that ensure sensitive data is wiped the moment it's no longer needed

We don't compromise on security to make features easier. We build features that work within our security model.

### The Right to Disappear

Your digital footprint shouldn't be permanent. With Cypherchat, you can:

- Create profiles without revealing your identity
- Communicate without leaving metadata trails
- Revoke connections and wipe conversations
- Export encrypted backups you control

**Privacy isn't about hiding—it's about having control over what you share, when you share it, and with whom.**

---

## 🚀 Getting Started

### For Users

#### 📱 Installation

Cypherchat is currently in **early alpha** (Foundation Phase). Public releases are planned for **Q2-Q3 2026**.

**When available, you'll be able to:**

1. **Download from GitHub Releases** – Signed APK with SHA-256 verification
2. **Install via F-Droid** – Reproducible builds for maximum trust
3. **Sideload manually** – For advanced users who want full control

**System Requirements:**

- Android 7.0 (API 24) or higher
- No Google Play Services required
- Works perfectly on de-Googled devices

#### 🎯 First Launch

1. **Create Your Profile** – The app automatically generates hardware-backed encryption keys. No passwords. No usernames. No accounts. Just launch and go.

2. **Get Your Invite Code** – Share a one-time SimpleX invite code (or scan a QR code) with contacts you want to message. Exchange it in person or through another secure channel.

3. **Verify Safety Words** – Compare the displayed safety words with your contact to confirm you're talking to the right person. This prevents man-in-the-middle attacks.

4. **Start Messaging** – Your conversations are encrypted end-to-end from the moment you send them. Messages are encrypted on your device, routed through SimpleX relays that can't read them, and decrypted only on your contact's device.

### For Developers

#### Prerequisites

- **Android Studio Hedgehog (2023.1.1)** or later
- **JDK 17**
- **Android SDK 26+** (target: API 34)
- **Git**

#### Quick Setup

```bash
# Clone the repository
git clone https://github.com/earnerbaymalay/e2eecc.git
cd e2eecc

# Build the project
./gradlew assembleDebug

# Run tests
./gradlew test

# Run lint checks
./gradlew lint
```

#### Project Structure

```text
Cypherchat/
├── app/                    # Main application module (Jetpack Compose UI)
├── core/
│   ├── common/            # Shared utilities, result types, dispatchers
│   ├── crypto/            # Android Keystore, encryption utilities
│   ├── database/          # SQLCipher encrypted database
│   └── network/           # SimpleX transport layer
├── feature/               # Feature modules (chat, contacts, settings)
└── gradle/                # Version catalog and wrapper
```

**Tech Stack:**

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material3)
- **Architecture**: MVVM + Clean Architecture
- **DI**: Koin
- **Database**: SQLCipher (encrypted SQLite)
- **Network**: SimpleX Protocol

---

## ✨ Feature Deep Dive

### Identity-Free Messaging

**What it means:** You don't need a phone number, email address, or username to use Cypherchat. Instead, you connect with contacts using **one-time SimpleX invite codes** that you share in person or through another secure channel.

**Why it matters:** Traditional messaging apps link your conversations to your real-world identity. Your phone number becomes your identity. Your email becomes your identity. Even if messages are encrypted, your identity isn't. Cypherchat breaks that link entirely. Even if someone intercepts your network traffic, they can't determine who you're messaging or build a profile of your contacts.

**How it works:** When you create a profile, Cypherchat generates a unique invite code. Share this code (or its QR representation) with someone you trust. Once they accept, you can message each other—but the servers routing your messages never know who either of you are. No accounts. No usernames. No identity to leak.

### Hardware-Backed Security

**What it means:** Your encryption keys are stored in your device's **Android Keystore**, a hardware-backed secure enclave that's isolated from the rest of your device's memory. Think of it as a digital safe that's built into your phone's hardware.

**Why it matters:** Software-based key storage can be extracted if your device is compromised. If someone gains root access to your device, they could potentially extract software-stored keys. Hardware-backed keys are protected by your device's Trusted Execution Environment (TEE), making them nearly impossible to extract even if an attacker gains root access. Your keys are protected by hardware, not just software.

**How it works:** When you first launch Cypherchat, the app generates a master key pair inside Android Keystore. This key pair is used to derive all other encryption keys, but the master keys themselves never leave the secure hardware. Even Cypherchat's own code can't read them directly—it can only request cryptographic operations using them. The keys exist in a hardware-protected space that even the operating system can't access directly.

### Zero-Knowledge Architecture

**What it means:** The servers that route your messages (SimpleX relays) have **zero knowledge** of:

- Who you are
- Who you're messaging
- What you're saying
- When you're online
- Your contact list

**Why it matters:** Most messaging apps store metadata (who messaged whom, when, from where) even if messages are encrypted. This metadata can reveal your entire social graph, communication patterns, and location history. A government or corporation can learn more about you from metadata than from message content. Cypherchat's zero-knowledge design means there's no metadata to leak. The servers literally cannot know anything about you or your communications.

**How it works:** Messages are encrypted on your device before transmission. They're routed through SimpleX relays that act as temporary message queues. These relays don't maintain accounts, don't store messages long-term, and can't correlate messages from the same user. Once a message is delivered, it's deleted from the relay. The relay is just a temporary mailbox—it doesn't know who owns it, who's sending to it, or what's inside.

### Metadata Minimization

**What it means:** Cypherchat minimizes the amount of information that can be inferred from your network traffic, even by sophisticated attackers performing traffic analysis.

**Why it matters:** Even encrypted messages leak metadata: timing patterns (when you're active), message sizes (how long your messages are), network paths (which servers you connect to). A sophisticated attacker can use this metadata to build a profile of your behavior, identify your contacts, and even infer what you're talking about. Cypherchat uses techniques to make this analysis significantly harder.

**How it works:** Messages are padded to standard sizes (so message length doesn't leak information), connections rotate through different SimpleX relays (so network paths can't be tracked), and the app doesn't maintain persistent connections that could be fingerprinted. Your communication patterns are obscured by design. You become harder to track, harder to profile, and harder to surveil.

### Encrypted Local Storage

**What it means:** Everything stored on your device—messages, contacts, settings—is encrypted using **SQLCipher**, an encrypted version of SQLite. The encryption keys are derived from your Android Keystore master keys.

**Why it matters:** If your device is lost, stolen, or seized, your data remains protected. Even with physical access to your device's storage, an attacker can't read your conversations without your device's hardware-backed keys. Your data is protected at rest, not just in transit.

**How it works:** Cypherchat uses SQLCipher to create an encrypted database. Before any data is written, it's encrypted. Before any data is read, it's decrypted. The database encryption key is derived from your Android Keystore keys and never stored in plaintext. Even if someone removes your device's storage and tries to read it directly, they'll only see encrypted data.

---

## 📚 Glossary

**Android Keystore** – A hardware-backed secure storage system on Android devices that protects cryptographic keys from extraction, even if the device is compromised. Keys stored here are protected by the device's Trusted Execution Environment (TEE).

**Double Ratchet** – A cryptographic protocol that provides forward secrecy and post-compromise security by constantly rotating encryption keys. Each message uses a new key, so compromising one key doesn't compromise past or future messages. (Planned for future releases)

**End-to-End Encryption (E2EE)** – Encryption where only the sender and recipient can decrypt messages. Not even the service provider can read them. Messages are encrypted on the sender's device and only decrypted on the recipient's device.

**Forward Secrecy** – A security property where past messages remain secure even if current encryption keys are compromised. Even if an attacker steals your keys today, they can't decrypt messages from yesterday.

**Hardware-Backed Keys** – Cryptographic keys stored in a device's Trusted Execution Environment (TEE), protected by hardware rather than software. These keys cannot be extracted even if the device is compromised at the software level.

**Invite Code** – A one-time code (or QR code) used to establish a connection between two Cypherchat users. No phone numbers or emails required. Share it in person or through another secure channel.

**Metadata** – Information about communications (who, when, where, how long) rather than the content itself. Often more revealing than message content. Metadata can reveal your social graph, communication patterns, and location history.

**Post-Compromise Security** – The ability to recover security after a key compromise by rotating to new keys. Even if an attacker steals your keys, you can generate new keys and continue communicating securely.

**Safety Words** – Human-readable words derived from cryptographic fingerprints, used to verify you're messaging the correct contact. Compare these words with your contact (in person or through another secure channel) to prevent man-in-the-middle attacks.

**SimpleX Protocol** – A decentralized messaging protocol that routes messages through temporary relays without requiring user accounts or central servers. Messages are routed through relays that don't know who you are.

**SQLCipher** – An encrypted version of SQLite that encrypts database files at rest, protecting data even if the device storage is accessed directly. All data is encrypted before being written to disk.

**Zero-Knowledge** – An architecture where the service provider has zero knowledge of user data, identities, or communication patterns. The servers routing your messages literally cannot know anything about you or your communications.

---

## 🗺️ Dev Roadmap

### Current Status: Early Alpha (Foundation Phase)

**Target General Availability: Q2-Q3 2026**

We're building Cypherchat in phases, prioritizing security and privacy at every step. Here's where we're going:

### Phase 1: Foundation (Current - December 2025) 🔨

**Goal**: Establish project structure, CI/CD, and base security infrastructure

- ✅ Project structure with core modules
- 🔄 Android Keystore integration
- 🔄 SQLCipher database setup
- 🔄 SimpleX transport integration
- 🔄 CI/CD pipeline

### Phase 2: Authentication & Transport (January-February 2026) 🔐

**Goal**: Establish secure user identity and message transport

- Profile creation with hardware-backed keys
- SimpleX invite code generation and QR display
- Fingerprint computation (safety words)
- Complete SimpleX transport layer

### Phase 3: Core Messaging (March-April 2026) 💬

**Goal**: Functional end-to-end encrypted messaging

- Chat list and conversation screens
- Message composition and encryption
- Double Ratchet state machine
- Contact management
- Message status indicators

### Phase 4: Advanced Features (May-June 2026) 🚀

**Goal**: Rich messaging and data management

- Image and file attachments
- Voice/audio notes
- Encrypted backup and restore
- Security settings (PIN/biometric lock)
- Message retention policies

### Phase 5: Security Hardening (July-August 2026) 🛡️

**Goal**: Production-grade security audit

- Third-party security audit
- Penetration testing
- Comprehensive testing suite
- Performance optimization

### Phase 6: Distribution & Release (September 2026) 📦

**Goal**: Public beta and stable release

- Reproducible builds
- F-Droid submission
- GitHub Releases with signed binaries
- **v1.0 Stable Release**

### Future Enhancements (Post-1.0) 🌟

- Message reactions and editing
- Quoted replies with threading
- Rich text formatting
- Interoperability bridges (Signal, Matrix)
- Enhanced accessibility features

**For detailed roadmap information, see [docs/ROADMAP.md](docs/ROADMAP.md)**

---

## 🤝 Contributing

We welcome contributions! Here's how to get started:

1. **Create an issue** first (bug report or feature request)
2. **Fork the repository** and create a branch: `feature/123-your-feature`
3. **Follow our coding standards** (see repository rules)
4. **Submit a Pull Request** using our template
5. **Wait for review** and ensure all checks pass

### Development Guidelines

- **Security First**: All code must pass security review
- **Test Coverage**: Aim for 95%+ unit test coverage on core modules
- **No Secrets**: Never commit secrets, API keys, or sensitive data
- **Memory Safety**: Clear sensitive data from memory after use
- **Code Review**: All PRs require security-cleared maintainer approval

**See [.github/CONTRIBUTING.md](.github/CONTRIBUTING.md) for detailed guidelines.**

---

## 📞 Support & Feedback

- **🐛 Bug Reports**: Use our [bug report template](.gitlab/issue_templates/bug.md)
- **💡 Feature Requests**: Use our [feature request template](.gitlab/issue_templates/feature.md)
- **🔒 Security Issues**: Create a [GitHub Security Advisory](https://github.com/earnerbaymalay/e2eecc/security/advisories/new) for responsible disclosure
- **💬 General Questions**: Open a GitHub Discussion (coming soon)

---

## 📄 License

**AGPL-3.0** – This project is licensed under the GNU Affero General Public License v3.0.

**Why AGPL?** We believe that security-critical software should remain open source. The AGPL ensures that any modifications or improvements to Cypherchat remain open and auditable, protecting users' privacy.

**Full license text**: [gnu.org/licenses/agpl-3.0](https://www.gnu.org/licenses/agpl-3.0.en.html)

---

## 🔗 Links

- **GitHub Repository**: [github.com/earnerbaymalay/e2eecc](https://github.com/earnerbaymalay/e2eecc)
- **SimpleX Protocol**: [github.com/simplex-chat/simplex-chat](https://github.com/simplex-chat/simplex-chat)
- **SQLCipher**: [zetetic.net/sqlcipher](https://www.zetetic.net/sqlcipher/)

---

**Built with ❤️ and 🔐 by the Cypherchat Team**

*Your privacy is not for sale. Your conversations are not for surveillance. Your data is yours.*
