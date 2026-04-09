# Cypherchat usage guide

Instructions for users and developers.

---

## For users

### Installation

1.  **Obtain the APK:** Download from [GitHub Releases](https://github.com/earnerbaymalay/e2eecc/releases) or build from source.
2.  **Install:** Enable "Install unknown apps" in Android settings, then install the APK.
3.  **Launch:** Open Cypherchat from your app drawer.

### First launch

1.  **Onboarding:** A brief introduction to Cypherchat's privacy features.
2.  **Key generation:** Your encryption keys are automatically generated via Android Keystore.
3.  **Chat list:** An empty chat list is displayed with "No conversations yet."

### Starting a conversation

1.  **Tap the '+' button** to generate an invitation link.
2.  **Share the link:** Securely send the link to your contact using an alternative, trusted channel (e.g., in person, encrypted email).
3.  **Await acceptance:** Once your contact accepts the invitation, the chat will appear.
4.  **Begin chatting:** Messages are automatically encrypted.

### Verifying a contact

This feature is planned for future implementation. When available:
1.  Open the conversation.
2.  Tap the contact's name in the title bar.
3.  Compare the key fingerprint with your contact, ideally in person or over a trusted channel.
4.  If the fingerprints match, mark the contact as "verified."

### Security best practices

-   🔒   **Verify key fingerprints** with frequently contacted individuals.
-   📱   **Use a screen lock** on your device; your keys are protected by the device's Trusted Execution Environment (TEE).
-   🔐   **Do not share invitation links publicly;** anyone with the link can connect to you.
-   🧹   **Delete conversations** you no longer need. Data is encrypted at rest, but minimizing stored data reduces risk.
-   📴   **Cypherchat works offline** for viewing past messages; no internet is required to read your history.

---

## For developers

### Building from source

```bash
git clone https://github.com/earnerbaymalay/e2eecc.git
cd e2eecc
```

Open the project in Android Studio:
1.  **File → Open** and select the `e2eecc` directory.
2.  **Sync Gradle** (typically automatic upon opening).
3.  **Run** on a device or emulator (Minimum SDK 24).

### Project structure

Refer to [ARCHITECTURE.md](ARCHITECTURE.md) for a comprehensive module breakdown.

### Running tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint checks
./gradlew lintDebug

# Build release APK
./gradlew assembleRelease
```

### Debugging cryptography

```bash
# Enable debug logging (for debug builds only)
adb shell setprop log.tag.Cypherchat VERBOSE

# View logs
adb logcat | grep Cypherchat
```

---

## Configuration

### Build variants

| Variant   | Use Case    | Features                                     |
|-----------|-------------|----------------------------------------------|
| `debug`   | Development | Debug logging, no minification, fast build.  |
| `release` | Production  | No logging, ProGuard, resource shrinking.    |

### Minimum requirements

-   Android 7.0+ (API 24)
-   100 MB free storage
-   2 GB RAM (recommended)

---

## Troubleshooting

See the separate `TROUBLESHOOTING.md` for common issues and solutions.

---

## Data and privacy

### What Cypherchat stores

-   **Encrypted messages:** Stored in an SQLCipher database, unreadable without your device.
-   **Your keys:** Stored in Android Keystore, non-exportable and hardware-bound.
-   **Contact public keys:** Essential for encryption, not considered secret.

### What Cypherchat does not store

-   Phone numbers
-   Usernames
-   Server accounts
-   Analytics or telemetry data
-   Crash reports (no external reporting)

### Uninstalling

When Cypherchat is uninstalled:
-   All messages are deleted (encrypted database is removed).
-   Keystore keys are orphaned (Android eventually cleans them up).
-   No data remains on any server.

---

Your data. Your device. Your control.
