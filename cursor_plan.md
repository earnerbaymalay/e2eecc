# Cursor Agent Execution Plan: Project Cypherchat

**Context:** Zero-Knowledge Android App (Kotlin/Compose).
**Strict Rules:** Follow `@security.md` and `@architecture.md` for every step.

## Phase 1: The Iron Skeleton (Project Setup)
- [ ] **Task 1.1: Dependency Management**
    - Create or update `gradle/libs.versions.toml`.
    - Add versions for: `androidx-core`, `compose-bom`, `kotlin`, `koin`, `sqlcipher`, `bip39`, `navigation-compose`.
    - **Rule:** Use stable versions only.

- [ ] **Task 1.2: Multi-Module Structure**
    - Edit `settings.gradle.kts` to include these modules:
      - `:app`
      - `:core:crypto`
      - `:core:database`
      - `:core:network`
      - `:core:common`
    - Create the physical directory structure and basic `build.gradle.kts` for each module.

- [ ] **Task 1.3: Build Logic**
    - Configure root `build.gradle.kts` to apply common plugins (Android Library, Kotlin) to subprojects so we don't repeat code.

## Phase 2: The Vault (Core Security)
*Note: Do not proceed until Phase 1 compiles.*

- [ ] **Task 2.1: Crypto Module Scaffolding**
    - In `:core:crypto`, create an interface `KeyManager`.
    - Implement `KeyManagerImpl` using Android Keystore System.
    - **Constraint:** Keys must be non-exportable and hardware-backed if available.

- [ ] **Task 2.2: BIP39 Implementation**
    - In `:core:crypto`, add a class `MnemonicManager`.
    - Implement `generateMnemonic()` returning a `CharArray` (NOT String).
    - Implement `validateMnemonic()`.
    - **Security:** Ensure memory is cleared after generation.

## Phase 3: The Database (Encrypted Storage)
- [ ] **Task 3.1: SQLCipher Setup**
    - In `:core:database`, set up the SQLCipher dependency.
    - Create a `DatabaseHelper` class that accepts a `Passphrase` (byte array).
    - **Constraint:** Ensure the DB is encrypted. Throw an error if an unencrypted open is attempted.

## Phase 4: The UI Shell (Compose)
- [ ] **Task 4.1: Navigation Graph**
    - In `:app`, set up `NavHost`.
    - Define routes: `OnboardingRoute`, `ChatListRoute`, `ChatDetailRoute`.

- [ ] **Task 4.2: Theme Engine**
    - Create `Theme.kt` in `:app`.
    - Implement the "Y2K/Matrix" color palette (Dark #000000, Terminal Green #00FF41).

## Phase 5: Verification
- [ ] **Task 5.1: Security Unit Tests**
    - Create a test in `:core:crypto` that proves keys cannot be extracted.
    - Create a test in `:core:database` that proves the DB file is unreadable without the key. 