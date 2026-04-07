# 🏗️ Architecture

## Overview

Cypherchat is a **multi-module Android application** built with Kotlin and Jetpack Compose. The architecture follows **Clean Architecture principles** with clear separation between UI, domain, and infrastructure layers.

```
┌─────────────────────────────────────────────────────┐
│                        app                          │
│              (UI + Navigation + DI)                 │
├─────────────┬─────────────┬────────────┬────────────┤
│ core:common │ core:crypto │ core:db    │ core:net   │
│  (Shared)   │  (Crypto)   │ (Storage)  │ (Transport)│
└─────────────┴─────────────┴────────────┴────────────┘
```

## Module Dependency Graph

```
                    ┌───── app ─────┐
                    │   (Compose)   │
                    └──┬──┬──┬──┬──┘
                       │  │  │  │
              ┌────────┘  │  │  └─────────┐
              ▼           ▼  ▼            ▼
    ┌─── core:common ──┐  │  ┌─ core:network ─┐
    │ (SecureResult,   │  │  │ (SimplexTrans-  │
    │  Logger,         │  │  │  port, Envelope)│
    │  Dispatchers)    │  │  └────────────────┘
    └─────┬───▲────────┘  │
          │   │           ▼
          │   │  ┌─ core:crypto ────┐
          │   └──│ (Keystore, AES,  │
          │      │  HKDF, Ratchet)  │
          │      └──────┬───────────┘
          │             │
          │             ▼
          │  ┌─ core:database ────┐
          └──│ (Room + SQLCipher, │
             │  Entities, DAOs)   │
             └────────────────────┘
```

## Module Descriptions

### `app` — UI Layer
- **Technology:** Jetpack Compose, Compose Navigation, Koin (DI)
- **Responsibility:** Screens, navigation, theme, dependency injection
- **Key Files:**
  - `CypherchatApplication.kt` — Application class, Koin module definitions
  - `MainNavigation.kt` — Compose NavHost with 3 routes
  - `ui/screen/` — OnboardingScreen, ChatListScreen, ConversationScreen
  - `ui/theme/` — Color palette, typography, theme composable

### `core:common` — Shared Utilities
- **Technology:** Kotlin coroutines
- **Responsibility:** Cross-cutting concerns shared by all modules
- **Key Types:**
  - `SecureResult<T>` — Error-handling type (Success/Failure) that avoids exception-based control flow in crypto code
  - `Logger` — Debug-only logger (no-op in release builds)
  - `DispatcherProvider` — Abstraction over coroutine dispatchers for testability

### `core:crypto` — Cryptographic Engine
- **Technology:** Java Cryptography Architecture, Android Keystore
- **Responsibility:** All cryptographic operations
- **Key Components:**

| Component | Algorithm | Role |
|---|---|---|
| `KeyStoreManager` | Android Keystore AES-256-GCM | Key generation, storage, retrieval |
| `AesGcmCipher` | AES-256-GCM (NoPadding) | Envelope encryption/decryption |
| `HkdfDerivation` | HKDF-SHA256 (RFC 5869) | Key derivation with memory zeroing |
| `DoubleRatchetState` | ECDH P-256 + HKDF | Signal-style double ratchet protocol |

**Envelope Format:**
```
[VERSION(1 byte)] [IV(12 bytes)] [CIPHERTEXT + AUTH TAG(variable)]
```

### `core:database` — Persistent Storage
- **Technology:** Room, SQLCipher
- **Responsibility:** Encrypted at-rest storage of messages and contacts
- **Key Components:**
  - `AppDatabase` — SQLCipher-encrypted Room database singleton
  - `MessageEntity` — Message schema (ciphertext stored as BLOB)
  - `ContactEntity` — Contact/peer identity records
  - `MessageDao` / `ContactDao` — Flow-based data access

**Encryption at Rest:**
- SQLCipher uses AES-256-CBC for the database file
- Passphrase derived from Android Keystore key (device-bound)
- Passphrase zeroed from memory immediately after database open

### `core:network` — Transport Layer
- **Technology:** Kotlin coroutines, Flow
- **Responsibility:** Message transport abstraction (SimpleX protocol)
- **Key Types:**
  - `SimplexTransport` — Interface defining connect, disconnect, send, receive
  - `MessageEnvelope` — Binary wire format for encrypted messages
  - `SimplexInvitation`, `SimplexConnection` — Connection lifecycle types

**Note:** The SimpleX SDK is not yet integrated. The interface is defined; a real implementation will either wrap the SimpleX Kotlin SDK or provide a local relay mock for development.

## Data Flow

### Message Send (future, fully wired)
```
User types message
        │
        ▼
ConversationScreen (Compose)
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

### Message Receive (future, fully wired)
```
SimplexTransport.receiveMessages()  →  Flow<IncomingEnvelope>
        │
        ▼
ConversationViewModel
        │
        ├──► DoubleRatchetState.decryptMessage()  →  plaintext
        │
        ├──► MessageDao.insert()  →  encrypted to SQLCipher DB
        │
        └──► UI update (Compose State)  →  MessageBubble displayed
```

## Dependency Injection (Koin)

```kotlin
// CypherchatApplication.kt
val coreModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}

val databaseModule = module {
    single { AppDatabase.getInstance(get()) }
    single { get<AppDatabase>().messageDao() }
    single { get<AppDatabase>().contactDao() }
}
```

**Planned modules:**
```kotlin
val cryptoModule = module {
    single { KeyStoreManager }
    single { DoubleRatchetFactory(get()) }
}

val networkModule = module {
    single<SimplexTransport> { SimplexTransportImpl(get()) }
}

val viewModelModule = module {
    viewModel { ConversationViewModel(get(), get(), get()) }
    viewModel { ChatListViewModel(get(), get()) }
}
```

## Design Decisions

### Why Multi-Module?
- **Crypto isolation** — `core:crypto` has no Android UI dependencies, making it auditable independently
- **Testability** — each module can be tested in isolation
- **Reusability** — crypto module could be extracted into a standalone library
- **Build times** — incremental builds only recompile changed modules

### Why SecureResult over Exceptions?
- **Explicit error handling** — the type system forces callers to handle failure
- **No exception overhead** in hot paths (encryption/decryption happens per-message)
- **Testable** — Success/Failure can be asserted directly

### Why SQLCipher + Keystore?
- **Defense in depth** — even if the device is compromised, the database requires the Keystore key
- **Hardware binding** — the database can only be opened on the device that created it
- **Zero configuration** — Android Keystore handles key rotation, TEE enforcement

### Why SimpleX?
- **No user IDs** — unlike Matrix, XMPP, or Signal, SimpleX doesn't require persistent identifiers
- **Metadata resistance** — the relay doesn't know who is talking to whom
- **Ephemeral queues** — each conversation uses temporary addresses

## Future Architecture Plans

- **ViewModel layer** — connect UI to real data sources
- **Repository pattern** — abstract data source (local DB vs. network)
- **X3DH handshake** — complete the key exchange protocol
- **SimpleX SDK integration** — real relay communication
- **Key verification UI** — fingerprint comparison for MITM protection
- **Message queuing** — offline message buffering and retry
