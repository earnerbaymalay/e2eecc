# Project Architecture: Cypherchat (e2eecc)

## 1. High-Level Topology
**Type:** Decentralized, Zero-Knowledge, Relay-Based Messaging Application.
**Model:** "Blind Relay" / "Dumb Pipe".
- **Client (App):** Handles 100% of logic, encryption, and data storage.
- **Server (Relay):** Dumb WebSocket/TCP relays that only route encrypted binary blobs. No business logic, no user database, no permanent storage.
- **Identity:** No phone numbers, no emails. Identity is a cryptographic Public Key.

## 2. Module Structure (Clean Architecture)
The project follows a strict multi-module architecture to enforce separation of concerns.

### :app
- **Role:** Dependency Injection (Koin/Hilt), Navigation, Theme, Entry Point.
- **Contains:** `MainActivity`, `NavGraph`, `Theme`.

### :core (The Foundation)
- **:core:crypto** -> Wraps Android Keystore, BIP39, and cryptographic primitives. **NO UI code.**
- **:core:database** -> SQLCipher implementation. Handles local persistence.
- **:core:network** -> SimpleX Core / Transport abstraction. Manages WebSocket/TCP connections.
- **:core:model** -> Shared data classes (e.g., `Message`, `Contact`) used across modules.

### :features (The UI Screens)
- **:feature:onboarding** -> Key generation, mnemonic display, PIN setup.
- **:feature:chat** -> Message list, composer, media handling.
- **:feature:settings** -> Security settings, panic button configuration, relay selection.

## 3. Data Flow & State
- **Pattern:** Unidirectional Data Flow (UDF) using Jetpack Compose and ViewModel.
- **Events:** UI -> ViewModel -> UseCase -> Repository -> DataSource.
- **State:** DataSource -> Repository -> ViewModel -> UI (StateFlow).

## 4. Transport Protocol (SMP - SimpleX Messaging Protocol)
- We do not use a central server API.
- We use unidirectional message queues.
- **Sending:** Client A posts encrypted blob to Client B's queue on a Relay.
- **Receiving:** Client B polls/subscribes to their queue on the Relay.

## 5. Tech Stack
- **Language:** Kotlin (Strict Mode).
- **UI:** Jetpack Compose (Material3).
- **Async:** Coroutines & Flow.
- **DI:** Koin (preferred) or Hilt.
- **Local DB:** SQLCipher (Encrypted SQLite).
- **Network:** OkHttp / SimpleX Core.