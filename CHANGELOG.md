# Changelog

All notable changes to Cypherchat are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0-alpha] - 2025-11-21

### Added

- Initial foundational project structure with core modules (`common`, `crypto`, `database`, `network`)
- MVVM + Clean Architecture scaffolding
- Security documentation and threat model
- Architecture documentation detailing module separation and patterns
- Contributing guidelines and code of conduct
- Cursor AI agent rules for security-first development
- Gradle build configuration with version catalog (`libs.versions.toml`)
- Android Manifest placeholders for all core modules

### Security

- Established zero-knowledge messaging principles
- Android Keystore integration plan for hardware-backed keys
- SQLCipher encrypted database support documented
- Memory safety guidelines (sensitive data wiping)
- Strict dependency management rules

### Documentation

- `README.md`: Project overview, quick start, and feature highlights
- `docs/ARCHITECTURE.md`: Module structure and Clean Architecture patterns
- `docs/SECURITY.md`: Security implementation checklist
- `docs/threat_model.md`: Threat scenarios and mitigations
- Development workflow guidelines with commit message conventions

### Future (Roadmap)

- Feature modules: `feature/chat`, `feature/contacts`, `feature/settings`
- Jetpack Compose UI implementation (Material3)
- Koin dependency injection setup
- SimpleX transport integration
- End-to-end encryption with Double Ratchet
- Secure backup and restore functionality
- CI/CD pipeline (GitHub Actions)
- Reproducible builds for F-Droid distribution

---

## Notes

- This is an **alpha release** for team coordination and foundation-setting
- No features are functional yet; all modules are structural scaffolding
- Heavy testing and security auditing will occur before beta
- Community security review process is planned post-beta

