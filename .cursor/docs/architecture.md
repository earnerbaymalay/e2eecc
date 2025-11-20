WQ# Cypherchat Architecture

## Overview
Cypherchat is a high-security, zero-knowledge messaging application built for Android using Kotlin and Jetpack Compose.

## Architecture Pattern
**MVVM + Clean Architecture**

### Module Structure
```
app/
├── core/
│   ├── database/      # SQLCipher encrypted database
│   ├── keystore/      # Android Keystore System integration
│   ├── network/       # SimpleX Core transport layer
│   └── security/      # Cryptographic utilities
├── feature/
│   ├── chat/          # Chat feature module
│   ├── contacts/      # Contact management
│   └── settings/      # App settings
└── app/               # Main application module
```

### Key Principles
- **No "God" Activities**: Each feature is self-contained
- **Dependency Injection**: Koin (preferred for modular builds)
- **Separation of Concerns**: Clear boundaries between layers
- **Atomic Features**: Smallest functional units possible

## Data Flow
1. **UI Layer**: Jetpack Compose screens
2. **ViewModel**: State management and business logic
3. **Repository**: Data access abstraction
4. **Data Sources**: Database, Network, Keystore

## Database
- **SQLCipher**: Encrypted SQLite database
- All sensitive data encrypted at rest
- Database key stored in Android Keystore

## Network
- **SimpleX Core**: Decentralized, no central servers
- End-to-end encryption at transport layer
- Zero-knowledge architecture

## Security Architecture
- Root keys: Android Keystore System (NOT EncryptedSharedPreferences)
- Sensitive data: Cleared from memory after use
- Random number generation: SecureRandom only
- No sensitive data in logs

