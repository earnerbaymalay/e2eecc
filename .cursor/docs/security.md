# Security Policy & Cryptographic Standards
**CRITICAL:** This project involves high-risk threat models. Security takes precedence over UX and Performance.

## 1. The "Zero-Knowledge" Rule
- The server (Relay) must NEVER receive plaintext.
- The OS (Android) must NEVER see plaintext keys in swap/disk.
- We collect **ZERO** telemetry. No Firebase Analytics, no Crashlytics, no Sentry.

## 2. Data Storage (At-Rest)
- **Database:** MUST use `SQLCipher` with a random 256-bit key.
- **Key Storage:**
  - Master Key is wrapped using **Android Keystore System** (TEE/StrongBox).
  - User PIN is used to unlock the Keystore entry.
  - NEVER store the Master Key in `SharedPreferences` or raw files.
- **Burner Mode:**
  - If `BurnerMode` is active, database is strictly `IN-MEMORY` (SQLite `:memory:`).
  - On app termination, memory is overwritten.

## 3. Data Transmission (In-Transit)
- **Layer 1:** Transport Layer Security (TLS 1.3) for the connection to the Relay.
- **Layer 2:** End-to-End Encryption (E2EE) payload using Double Ratchet Algorithm (Signal/SimpleX).
- **Metadata Protection:** We do not send sender/receiver IDs in the clear.

## 4. Memory Hygiene
- **Sensitive Data:** Passwords, Seed Phrases (BIP39), and Private Keys.
- **Handling:**
  - Use `CharArray` or `byte[]` instead of `String` where possible.
  - Explicitly clear (zero-out) arrays after use: `Arrays.fill(secret, (byte) 0)`.
- **Screenshots:** `FLAG_SECURE` must be enabled on all Activities by default.

## 5. Entropy & Randomness
- **FORBIDDEN:** `java.util.Random`, `Math.random()`.
- **REQUIRED:** `java.security.SecureRandom` (Linux PRNG).

## 6. Logging & Debugging
- **Strict Rule:** NEVER log variable content in Release builds.
- **Logcat:** Use a custom `Timber` tree that creates no-ops in Release builds.
- **ProGuard/R8:** Obfuscation must be enabled aggressively in Release builds.

## 7. Forbidden Android APIs
- `PreferenceManager.getDefaultSharedPreferences` (Use `EncryptedSharedPreferences` or SQLCipher).
- `android.util.Log` (Direct usage forbidden, use secure wrapper).
- `WebView` (Forbidden due to massive attack surface, unless strictly sandboxed for rendering specific content).