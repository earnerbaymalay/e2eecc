# Cypherchat Development Roadmap

**Status**: Early Alpha (Foundation Phase)
**Last Updated**: April 2026
**Target GA**: Q2-Q3 2026

---

## 🎯 Strategic Goals

1. **Zero-Knowledge Architecture**: Complete end-to-end encryption pipeline with no central server involvement
2. **Hardware-Backed Security**: All long-term secrets in Android Keystore with SQLCipher at-rest encryption
3. **Identity-Free Messaging**: SimpleX transport enabling metadata-minimal communication
4. **Production-Ready Codebase**: Comprehensive testing, security auditing, and documentation
5. **Open Roadmap**: Transparent development with community input on security decisions

---

## 📋 Phase 1: Foundation (✅ Complete — April 2026)

**Goal**: establish project structure, CI/CD, and base security infrastructure

### Core Modules

- [x] Project structure with `app`, `core/*`, and `gradle` directories
- [x] **crypto** module
  - [x] Android Keystore integration (key generation, storage, retrieval)
  - [x] SecureRandom implementation for all cryptographic operations
  - [x] Encryption/decryption utilities (AES-GCM)
  - [x] Double Ratchet algorithm skeleton
  - [x] HKDF key derivation (RFC 5869)
  - [x] Unit tests for AesGcmCipher and HkdfDerivation

- [x] **database** module
  - [x] SQLCipher configuration and initialization
  - [x] Room ORM setup with encrypted database
  - [x] Message, Contact DAOs with Flow-based queries
  - [x] TypeConverters for dates and enums

- [x] **network** module
  - [x] SimpleX transport interface definition
  - [x] Message envelope wire format
  - [x] Connection lifecycle types

- [x] **common** module
  - [x] SecureResult<T> sealed class for error handling
  - [x] Coroutine dispatchers (DispatcherProvider)
  - [x] Logger abstraction (no sensitive data logging)

### Infrastructure

- [x] GitHub Actions CI/CD pipeline (build, test, lint)
- [x] GitLab CI pipeline (security scanning)
- [x] Dependency management with `libs.versions.toml`
- [x] ProGuard rules for release builds
- [x] ProGuard rules for release builds

---

## 📋 Phase 2: Authentication & Transport (May-June 2026)

**Goal**: Establish secure user identity and message transport

### Profile & Identity Management

- [ ] **app** module UI (Compose)
  - [ ] Onboarding screen
  - [ ] Profile creation with hardware-backed key provisioning
  - [ ] SimpleX invite code generation
  - [ ] QR code display for invite sharing

- [ ] **core/crypto** enhancements
  - [ ] X3DH handshake protocol
  - [ ] Fingerprint computation (safety words from key hash)
  - [ ] Seed phrase management (12-word recovery)

### SimpleX Integration

- [ ] Complete SimpleX transport layer
  - [ ] Connection establishment
  - [ ] Message send/receive pipeline
  - [ ] Connection revocation
  - [ ] Metadata minimization verification

---

## 📋 Phase 3: Core Messaging (July-August 2026)

**Goal**: Functional end-to-end encrypted messaging with contact management

### Chat Feature

- [ ] **UI Components**
  - [x] Chat list screen (skeleton)
  - [x] Conversation screen with message bubbles (skeleton)
  - [ ] Wire ViewModels to real data sources
  - [ ] Message status indicators (sent, delivered, read)

- [ ] **Data Layer**
  - [ ] Message repository wrapping DAOs
  - [ ] Local message caching
  - [ ] Message pagination for large conversations
  - [ ] Encryption before database storage (wire ConversationViewModel)

- [ ] **Business Logic**
  - [ ] Message composition and encryption
  - [x] Double Ratchet state machine (implemented, needs network wiring)
  - [ ] Message ordering and deduplication

---

## 📋 Phase 4: Advanced Features (September-October 2026)

**Goal**: Rich messaging and data management capabilities

### Media Support

- [ ] Image attachment sending (compression before encryption)
- [ ] File attachment support with integrity verification
- [ ] Voice/audio notes

### Settings & Account Management

- [ ] Backup and restore (SQLCipher database export)
- [ ] Encrypted backup file format
- [ ] PIN/Biometric lock
- [ ] Message retention policies and auto-delete timers

### Search & Indexing

- [ ] Local message search (encrypted)
- [ ] Contact search

---

## 📋 Phase 5: Security Hardening & Testing (November-December 2026)

**Goal**: Production-grade security audit and comprehensive testing

### Security Review

- [ ] Third-party security audit
- [ ] Penetration testing
- [ ] Cryptographic review of Double Ratchet implementation
- [ ] Dependency scanning and SCA

### Testing

- [ ] Integration tests (50+ scenarios)
- [ ] End-to-end tests on emulator/device
- [ ] Performance benchmarks
- [ ] Memory leak detection

---

## 📋 Phase 6: Distribution & Release (Q1 2027)

**Goal**: Public beta and eventual stable release

### Build & Distribution

- [ ] Reproducible builds
- [ ] F-Droid submission
- [ ] GitHub Releases with signed binaries
- [ ] Automated delta updates

### Community

- [ ] Security advisory disclosure process
- [ ] Bug bounty program
- [ ] Community forum/discussions
- [ ] Regular security updates

---

## 🚀 Future Enhancements (Post-1.0)

- [ ] Signal protocol bridge (potential)
- [ ] Matrix federation (potential)
- [ ] Post-quantum key exchange research
- [ ] Rich text formatting in messages
- [ ] Quoted replies with threading

---

## 🔐 Security Priorities Throughout All Phases

- ✅ **Memory Safety**: Overwrite sensitive data after use in every phase
- ✅ **No Logging**: Sanitize all logging; never log PII or cryptographic material
- ✅ **Secure Dependencies**: Pin exact versions; evaluate every new library
- ✅ **Code Review**: All PRs reviewed by security-cleared maintainers
- ✅ **Threat Modeling**: Revisit threat model quarterly as features evolve

---

**Built with ❤️ and 🔐 by the Cypherchat Team**
