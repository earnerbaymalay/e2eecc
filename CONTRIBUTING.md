# Contributing to Cypherchat

Thank you for considering a contribution. **Security software is only as good as its review**, and every pair of eyes makes Cypherchat stronger.

## 🧭 Getting Started

1. Fork the repository
2. Clone your fork: `git clone https://github.com/YOUR_USERNAME/e2eecc.git`
3. Open in Android Studio (Arctic Fox+)
4. Sync Gradle and ensure the project builds
5. Create a branch: `git checkout -b feature/your-feature-name`

## 📐 Coding Standards

### Kotlin
- Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use `SecureResult<T>` instead of exceptions for crypto operations
- Zero sensitive data from memory after use (`ByteArray.fill(0)`)
- Never log sensitive data — use `Logger.d()` only for non-sensitive debug info
- All crypto code must include security comments explaining the *why*

### Architecture
- New code goes in the appropriate `core/` module
- UI changes go in `app/src/main/java/com/cypherchat/ui/`
- Never import crypto libraries across module boundaries — use the interfaces in `core/crypto/`
- Use Koin for dependency injection — no `object` singletons for services

### Commits
- Use [Conventional Commits](https://www.conventionalcommits.org/):
  - `feat: add key verification UI`
  - `fix: prevent IV reuse in AesGcmCipher`
  - `docs: update threat model`
  - `refactor: extract DoubleRatchet into separate module`

### Security-Specific Rules
- **No hardcoded secrets** — ever
- **No logging of keys, IVs, or ciphertexts** in any build
- **No weakening of crypto** for convenience (e.g., static IVs for testing)
- **All new crypto code requires security review** before merge
- **Benchmark before merging** — crypto operations must not block the main thread

## 🧪 Testing

- Unit tests go in `src/test/`
- Instrumentation tests go in `src/androidTest/`
- All crypto code must have tests for:
  - Encryption → decryption round-trip
  - Key derivation determinism
  - Edge cases (empty input, maximum input, invalid input)
  - Memory zeroing verification (where applicable)

Run tests:
```bash
./gradlew test          # Unit tests
./gradlew connectedAndroidTest  # Instrumentation tests
```

## 🔐 Contributing to Crypto Code

If you're modifying `core/crypto/`:

1. **Explain the cryptographic rationale** in your PR description
2. **Reference standards** (NIST SP 800-38D for GCM, RFC 5869 for HKDF, etc.)
3. **Provide test vectors** where possible
4. **Consider side-channel attacks** (timing, power analysis)
5. **Document any deviations** from standard protocols

## 📋 Pull Request Process

1. Ensure all tests pass: `./gradlew test`
2. Run lint: `./gradlew lintDebug`
3. Update documentation if your change affects architecture or security
4. Request review from a maintainer
5. Address review feedback promptly

## 🛡️ Responsible Disclosure

Found a security issue? See [SECURITY.md](SECURITY.md) for our responsible disclosure process.

## 💬 Community

- **GitHub Issues:** Bug reports, feature requests
- **GitHub Discussions:** Architecture questions, crypto debates
- **Code of Conduct:** [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)

## 🎯 Where to Start

Good first contributions:
- 🐛 Fix bugs in the UI placeholder screens
- 📝 Improve documentation
- 🧪 Add unit tests to crypto modules
- 🎨 Refine the Compose theme
- 📊 Performance profiling of crypto operations

For experienced cryptographers:
- 🔍 Review and improve the Double Ratchet implementation
- 📐 Add formal verification of cryptographic properties
- 🔑 Integrate a proper X3DH handshake protocol
- 🛡️ Conduct a full security audit

---

*Cypherchat exists because privacy is a right, not a feature.*
