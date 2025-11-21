# 🔐 Cypherchat

**Zero-Knowledge, End-to-End Encrypted Messaging**

[![Source](https://img.shields.io/badge/source-github-blue?logo=github)](https://github.com/earnerbaymalay/e2eecc)
[![License](https://img.shields.io/badge/license-AGPL--3.0-blue.svg)](https://www.gnu.org/licenses/agpl-3.0.en.html)

## 📚 Table of Contents
- [Highlights](#-highlights)
- [Quick Start](#-quick-start)
- [Releases & Downloads](#-releases--downloads)
- [Day-to-Day Flow](#-day-to-day-flow)
- [Architecture](#-architecture)
- [Security Principles](#-security-principles)
- [Development Workflow](#-development-workflow)
- [Testing](#-testing)
- [CI/CD Pipeline](#-cicd-pipeline)
- [Issue Templates](#-issue-templates)
- [Contributing](#-contributing)
- [FAQ](#-faq)
- [Support & Feedback](#-support--feedback)
- [License](#-license)
- [Links](#-links)

---

## 🌟 Highlights

- **Identity-free messaging**: Connect via SimpleX invite codes—no phone numbers, emails, or usernames to leak.
- **Hardware-backed keys**: All long-term secrets live in Android Keystore; message stores stay encrypted with SQLCipher.
- **Metadata-minimised transport**: Messages route through SimpleX relays without account identifiers or IP correlation.
- **Secure by design**: Kotlin, Jetpack Compose, and strict MVVM/Clean layering keep responsibilities isolated and easier to audit.
- **Open roadmap**: Feature modules (chat, contacts, settings) evolve in the open so you can verify each security decision.

---

## 🚀 Quick Start

### Prerequisites
- **Android Studio Hedgehog (2023.1.1)** or later
- **JDK 17**
- **Android SDK 26+** (target: API 34)
- **Git**

### Clone & Build
```bash
git clone https://github.com/earnerbaymalay/e2eecc.git
cd e2eecc
./gradlew assembleDebug
```

---

## 📦 Releases & Downloads

> Signed binaries are not published yet. Use the placeholders below to plan verification steps; once the first public build ships we will replace every `TBD` with concrete artifacts, hashes, and signatures.

| Channel | Status | Download | Notes |
| --- | --- | --- | --- |
| Stable | Coming soon | `TBD (GitHub Releases link)` | Signed with the Cypherchat release keystore; SHA-256 + sig posted in release notes |
| Preview | Planned | `TBD (GitHub Pre-release)` | Opt-in builds for community testers; may include experimental features |

### Manual Install (Sideload)
1. Download `Cypherchat-vX.Y.Z.apk` from the appropriate channel above.
2. Verify the SHA-256 checksum published in the release notes:
   ```powershell
   Get-FileHash .\Cypherchat-vX.Y.Z.apk -Algorithm SHA256
   ```
   ```bash
   shasum -a 256 Cypherchat-vX.Y.Z.apk
   ```
3. (Coming soon) Verify the detached signature with the Cypherchat release key fingerprint `TBD`.
4. Install via ADB:
   ```bash
   adb install --prompt-permissions Cypherchat-vX.Y.Z.apk
   ```
5. On-device: enable “Install unknown apps” for your file manager, open the APK, and confirm the fingerprint matches the value posted in the release notes.

### Future Distribution
- **F-Droid / Aurora Store**: Evaluation in progress; once reproducible builds are wired up we’ll document the submission process here.
- **Automated delta updates**: Planned after stable 1.0 to minimize download sizes while keeping the zero-knowledge guarantees intact.

---

## 📲 Day-to-Day Flow

1. **Create a profile**: On first launch the app provisions hardware-backed keys and a local, encrypted profile.
2. **Exchange invites**: Share a one-time SimpleX code or QR with your contact in person or via another secure channel.
3. **Verify fingerprints**: Compare the displayed safety words to confirm you’re talking to the right person.
4. **Chat & share**: Send text, attachments, or voice notes (feature roadmap) knowing nothing leaves your devices unencrypted.
5. **Manage sessions**: Revoke old links, wipe conversation states, or export encrypted backups from the settings module (coming soon).

---

## 🏗️ Architecture

```
Cypherchat/
├── app/               # Compose UI, navigation, app entry point
├── core/
│   ├── common/        # Shared utilities, result types, dispatchers
│   ├── crypto/        # Keystore, encryption, SecureRandom helpers
│   ├── database/      # SQLCipher config, DAOs
│   └── network/       # SimpleX transport & networking
└── gradle/            # Version catalog and wrapper metadata
```

Feature modules (`feature/chat`, `feature/contacts`, etc.) will live beside `core` as they are implemented.

**Pattern**: MVVM + Clean Architecture  
**DI**: Koin  
**UI**: Jetpack Compose (Material3)

---

## 🔒 Security Principles

1. **Zero-Knowledge**: Server never sees plaintext messages or metadata
2. **Android Keystore**: Root keys stored in hardware-backed secure storage
3. **SQLCipher**: All local data encrypted at rest
4. **SimpleX**: No phone numbers, no central identity
5. **Memory Safety**: Sensitive data cleared immediately after use

---

## 🛠️ Development Workflow

### Branching Strategy
- `main` → Production releases (protected)
- `develop` → Integration branch
- `feature/*` → New features
- `bugfix/*` → Bug fixes
- `hotfix/*` → Emergency production fixes

### Commit Messages
```
feat(chat): add message reactions
fix(crypto): clear key material from memory
security(keystore): migrate to hardware-backed keys
```

### Code Review Checklist
Before submitting a Pull/Merge Request, ensure (see the [PR template](.github/pull_request_template.md)):
- [ ] No secrets in code or logs
- [ ] Security checklist passed
- [ ] Unit tests added
- [ ] Lint warnings resolved
- [ ] `@Preview` composables for UI changes

---

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Lint
./gradlew lint

# Build all variants
./gradlew assembleDebug assembleRelease
```

---

## 📦 CI/CD Pipeline

Automated CI config has not been committed yet. Run the Gradle commands in the testing section locally before opening a PR/MR. Once the workflow (GitHub Actions or GitLab CI) lands in the repo, this section will describe each stage and link to the configuration file.

---

## 📝 Issue Templates

- **Bug Report**: [`.github/ISSUE_TEMPLATE/bug_report.md`](.github/ISSUE_TEMPLATE/bug_report.md)
- **Feature Request**: [`.github/ISSUE_TEMPLATE/feature_request.md`](.github/ISSUE_TEMPLATE/feature_request.md)

---

## 🤝 Contributing

1. Create an issue first (bug or feature)
2. Fork & create a branch: `feature/123-your-feature`
3. Follow coding standards (see repo rules)
4. Submit a Pull/Merge Request using the [template](.github/pull_request_template.md)
5. Wait for review & local checks to pass

---

## ❓ FAQ

**Do I need a phone number or email to sign up?**  
No. Cypherchat relies on SimpleX invite codes, so your real-world identity never touches the transport layer.

**Where are my chats stored?**  
Only on your device inside an SQLCipher-encrypted database, additionally protected by Android Keystore–derived keys.

**What if I lose my device?**  
Your data stays encrypted. Reinstall Cypherchat on a new device, create a fresh profile, and re-exchange invite codes. Encrypted backup exports are on the roadmap.

**Does Cypherchat work without Google Play Services?**  
Yes. It’s built with pure Kotlin and Compose; binaries can be sideloaded as long as the OS meets the minimum SDK level.

**Is the project open source?**  
Absolutely. The full codebase lives on GitHub under AGPL-3.0 so you can audit every commit.

---

## 📬 Support & Feedback

- **Bug reports & features**: Use the GitHub issue templates linked above so we capture environment and security details.
- **General questions**: Open a discussion thread (coming soon) or reach out via issue comments.
- **Responsible disclosure**: Until a dedicated SECURITY policy ships, create a GitHub Security Advisory draft so maintainers can coordinate privately.

---

## 📄 License

**AGPL-3.0** – Full text: [gnu.org/licenses/agpl-3.0](https://www.gnu.org/licenses/agpl-3.0.en.html). A local `LICENSE` file will be added before the first public release.

---

## 🔗 Links

- **GitHub**: https://github.com/earnerbaymalay/e2eecc
- **SimpleX Docs**: https://github.com/simplex-chat/simplex-chat
- **SQLCipher**: https://www.zetetic.net/sqlcipher/

---

**Built with ❤️ and 🔐 by the Cypherchat Team**
