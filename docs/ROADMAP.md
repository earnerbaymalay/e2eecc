# Cypherchat Development Roadmap

**Status**: Early Alpha (Foundation Phase)  
**Last Updated**: November 21, 2025  
**Target GA**: Q2-Q3 2026

---

## 🎯 Strategic Goals

1. **Zero-Knowledge Architecture**: Complete end-to-end encryption pipeline with no central server involvement
2. **Hardware-Backed Security**: All long-term secrets in Android Keystore with SQLCipher at-rest encryption
3. **Identity-Free Messaging**: SimpleX transport enabling metadata-minimal communication
4. **Production-Ready Codebase**: Comprehensive testing, security auditing, and documentation
5. **Open Roadmap**: Transparent development with community input on security decisions

---

## 📋 Phase 1: Foundation (Current - December 2025)

**Goal**: Establish project structure, CI/CD, and base security infrastructure

### Core Modules

- [x] Project structure with `app`, `core/*`, and `gradle` directories
- [ ] **crypto** module
  - [ ] Android Keystore integration (key generation, storage, retrieval)
  - [ ] SecureRandom implementation for all cryptographic operations
  - [ ] Encryption/decryption utilities (AES-GCM)
  - [ ] Double Ratchet algorithm skeleton
  - [ ] Sensitive memory wiping utilities
  - [ ] Unit tests (95%+ coverage)

- [ ] **database** module
  - [ ] SQLCipher configuration and initialization
  - [ ] Room ORM setup with encrypted database
  - [ ] Message, Contact, and Session DAOs
  - [ ] Database migration strategy
  - [ ] Unit tests for encryption/decryption round-trips

- [ ] **network** module
  - [ ] SimpleX transport client integration
  - [ ] Protocol buffer definitions for messages
  - [ ] Network request/response models
  - [ ] Error handling and retry logic
  - [ ] Integration tests with mock SimpleX

- [ ] **common** module
  - [ ] Result<T> sealed class for error handling
  - [ ] Coroutine dispatchers (Main, IO, Default)
  - [ ] Shared utilities and extensions
  - [ ] Logger abstraction (no sensitive data logging)

### Infrastructure

- [ ] GitHub Actions CI/CD pipeline
  - [ ] Build on every PR
  - [ ] Run all unit tests
  - [ ] Lint checks (Detekt, Android Lint)
  - [ ] Security scanning (Dependency Check)
- [ ] Dependency management with `libs.versions.toml`
- [ ] Detekt configuration for code quality

---

## 📋 Phase 2: Authentication & Transport (January-February 2026)

**Goal**: Establish secure user identity and message transport

### Profile & Identity Management

- [ ] **app** module UI (Compose)
  - [ ] Onboarding screen
  - [ ] Profile creation with hardware-backed key provisioning
  - [ ] SimpleX invite code generation
  - [ ] QR code display for invite sharing
  - [ ] @Preview composables for all screens

- [ ] **core/crypto** enhancements
  - [ ] HKDF for key derivation
  - [ ] Fingerprint computation (safety words from key hash)
  - [ ] Seed phrase management (12-word recovery)

### SimpleX Integration

- [ ] Complete SimpleX transport layer
  - [ ] Connection establishment
  - [ ] Message send/receive pipeline
  - [ ] Connection revocation
  - [ ] Metadata minimization verification

- [ ] Protocol implementation
  - [ ] Message envelope structure
  - [ ] Delivery confirmation acknowledgments
  - [ ] Typing indicators (privacy-aware)

---

## 📋 Phase 3: Core Messaging (March-April 2026)

**Goal**: Functional end-to-end encrypted messaging with contact management

### Chat Feature Module (`feature/chat`)

- [ ] **UI Components**
  - [ ] Chat list screen
  - [ ] Conversation screen with message bubbles
  - [ ] Message input with send button
  - [ ] Message reactions and editing UI
  - [ ] Message status indicators (sent, delivered, read)

- [ ] **Data Layer**
  - [ ] Message repository and DAOs
  - [ ] Local message caching
  - [ ] Message pagination for large conversations
  - [ ] Encryption before database storage

- [ ] **Business Logic**
  - [ ] Message composition and encryption
  - [ ] Double Ratchet state machine
  - [ ] Message ordering and deduplication
  - [ ] Conversation state management (Koin)

### Contacts Feature Module (`feature/contacts`)

- [ ] Contact list UI
- [ ] Contact detail screen
- [ ] Contact request workflow
- [ ] Contact verification (fingerprint comparison)
- [ ] Contact blocking/muting

---

## 📋 Phase 4: Advanced Features (May-June 2026)

**Goal**: Rich messaging and data management capabilities

### Media Support

- [ ] Image attachment sending
  - [ ] Compression before encryption
  - [ ] Thumbnail generation
  - [ ] Download and storage

- [ ] File attachment support
  - [ ] Integrity verification (SHA-256)
  - [ ] Size limits and warnings

- [ ] Voice/audio notes (planned)
  - [ ] Recording and encoding
  - [ ] Playback with progress indicator

### Settings & Account Management (`feature/settings`)

- [ ] Backup and restore
  - [ ] SQLCipher database export
  - [ ] Encrypted backup file format
  - [ ] Restore workflow

- [ ] Security settings
  - [ ] PIN/Biometric lock
  - [ ] Session timeout configuration
  - [ ] Key rotation scheduling

- [ ] Privacy controls
  - [ ] Message retention policies
  - [ ] Auto-delete timers
  - [ ] Metadata minimization status

### Search & Indexing

- [ ] Local message search (encrypted)
- [ ] Contact search
- [ ] Search result pagination

---

## 📋 Phase 5: Security Hardening & Testing (July-August 2026)

**Goal**: Production-grade security audit and comprehensive testing

### Security Review

- [ ] Third-party security audit (TBD: firm selection)
- [ ] Penetration testing
- [ ] Cryptographic review
- [ ] Dependency scanning and SCA

### Testing

- [ ] Integration tests (50+ scenarios)
- [ ] End-to-end tests on emulator/device
- [ ] Performance benchmarks
- [ ] Stress testing (large message volumes)
- [ ] Memory leak detection

### Documentation

- [ ] Security implementation details
- [ ] API documentation for future SDK
- [ ] User guide and FAQ updates
- [ ] Contributor security handbook

---

## 📋 Phase 6: Distribution & Release (September 2026)

**Goal**: Public beta and eventual stable release

### Build & Distribution

- [ ] Reproducible builds
- [ ] F-Droid submission
- [ ] Aurora Store listing
- [ ] GitHub Releases with signed binaries
- [ ] Automated delta updates

### Community

- [ ] Security advisory disclosure process
- [ ] Bug bounty program (pending)
- [ ] Community forum/discussions
- [ ] Regular security updates

### Release Candidates

- [ ] Beta 1: Community testing
- [ ] Beta 2-3: Feedback integration
- [ ] Release Candidate: Final security review
- [ ] **v1.0 Stable**: Feature-complete, audited, production-ready

---

## 🚀 Future Enhancements (Post-1.0)

### Messaging UX

- [ ] Message reactions emoji picker
- [ ] Quoted replies with threading
- [ ] Message-editing history
- [ ] Unsend capability (within time window)
- [ ] Rich text formatting

### Interoperability

- [ ] Signal protocol bridge (potential)
- [ ] Matrix federation (potential)
- [ ] Portable key export (QR/file-based migration)

### Performance

- [ ] Lazy loading for media
- [ ] Encrypted caching strategies
- [ ] Background sync optimization
- [ ] Battery usage optimization

### Accessibility

- [ ] Screen reader support
- [ ] High-contrast themes
- [ ] Font size scaling
- [ ] Gesture support for power users

---

## 🔐 Security Priorities Throughout All Phases

- ✅ **Memory Safety**: Overwrite sensitive data after use in every phase
- ✅ **No Logging**: Sanitize all logging; never log PII or cryptographic material
- ✅ **Secure Dependencies**: Pin exact versions; evaluate every new library
- ✅ **Code Review**: All PRs reviewed by security-cleared maintainers
- ✅ **Threat Modeling**: Revisit threat model quarterly as features evolve

---

## 📊 Key Metrics & Success Criteria

| Phase | Metric | Target |
|-------|--------|--------|
| Phase 1 | Core module coverage | 95%+ unit tests |
| Phase 2 | Profile creation time | < 2 seconds |
| Phase 3 | E2E message latency | < 500ms average |
| Phase 4 | Media encryption overhead | < 5% |
| Phase 5 | Security audit findings | 0 critical issues |
| Phase 6 | Beta user count | 1,000+ testers |

---

## 🤝 Community Involvement

- **Phase 1-2**: Closed core team (security-critical foundation)
- **Phase 3-4**: Limited beta (selected security researchers)
- **Phase 5**: Expanded security review program
- **Phase 6+**: Full open-source community contributions

---

## 📞 Feedback & Adjustments

This roadmap is **living** and subject to change based on:

- Security discoveries or new threat vectors
- Community feature requests
- Dependency availability/lifecycle
- Resource allocation

File issues or discussions on GitHub to propose changes.

---

**Built with ❤️ and 🔐 by the Cypherchat Team**

