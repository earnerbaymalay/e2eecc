# Usage Guide

## Getting Started

### For Users

#### Installation
1. **Get the APK** — Download from [GitHub Releases](https://github.com/earnerbaymalay/e2eecc/releases) or build from source
2. **Install** — Allow "Install unknown apps" in Android settings, then install the APK
3. **Launch** — Open Cypherchat

#### First Launch
1. **Onboarding** — You'll see a brief intro to Cypherchat's privacy features
2. **Key generation** — Your encryption keys are generated automatically via Android Keystore
3. **Chat list** — You'll see an empty chat list with "No conversations yet"

#### Starting a Conversation
1. **Tap the + button** to create an invitation link
2. **Share the link** securely with your contact (in person, via a different channel, or encrypted email)
3. **Wait for them to accept** — once they accept the invitation, the chat appears
4. **Start chatting** — messages are encrypted automatically

#### Verifying a Contact
> ⚠️ This feature is planned but not yet implemented.

When key verification is available:
1. Open the conversation
2. Tap the contact name in the title bar
3. Compare the key fingerprint with your contact (in person or via a trusted channel)
4. If they match, mark the contact as "verified"

#### Security Best Practices
- 🔒 **Verify key fingerprints** with contacts you communicate with regularly
- 📱 **Use a screen lock** on your device — your keys are protected by the device's TEE
- 🔐 **Don't share invitation links publicly** — anyone with the link can connect to you
- 🧹 **Delete conversations** you no longer need — data is encrypted at rest, but less data = less risk
- 📴 **Cypherchat works offline** for viewing past messages — no internet needed to read your history

### For Developers

#### Building from Source
```bash
git clone https://github.com/earnerbaymalay/e2eecc.git
cd e2eecc
```

Open in Android Studio:
1. **File → Open** → select the `e2eecc` directory
2. **Sync Gradle** (automatic on open)
3. **Run** on a device or emulator (Min SDK 24)

#### Project Structure
See [ARCHITECTURE.md](ARCHITECTURE.md) for the complete module breakdown.

#### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lintDebug

# Build release APK
./gradlew assembleRelease
```

#### Debugging Crypto
```bash
# Enable debug logging (only in debug builds)
adb shell setprop log.tag.Cypherchat VERBOSE

# View logs
adb logcat | grep Cypherchat
```

## Configuration

### Build Variants
| Variant | Use Case | Features |
|---|---|---|
| `debug` | Development | Debug logging, no minification, fast build |
| `release` | Production | No logging, ProGuard, resource shrinking |

### Minimum Requirements
- Android 7.0+ (API 24)
- 100 MB free storage
- 2 GB RAM recommended

## Troubleshooting

| Problem | Solution |
|---|---|
| App crashes on launch | Check logcat for errors. Ensure Android Keystore is available (not a custom ROM with broken Keystore) |
| Can't create invitation | Network required for SimpleX relay connection. Check internet |
| Messages not sending | Alpha limitation — network layer not yet wired. This will show a placeholder error |
| Database corruption | Reinstall. Database is tied to your device Keystore — can't be migrated |
| Build fails | Ensure JDK 17, Android SDK 34, and sync Gradle |

## Data and Privacy

### What Cypherchat Stores
- **Encrypted messages** — in SQLCipher database (unreadable without your device)
- **Your keys** — in Android Keystore (non-exportable, hardware-bound)
- **Contact public keys** — needed for encryption (not secret)

### What Cypherchat Does NOT Store
- Phone numbers
- Usernames
- Server accounts
- Analytics or telemetry
- Crash reports (no external reporting)

### Uninstalling
When you uninstall Cypherchat:
- All messages are deleted (encrypted database is removed)
- Keystore keys are orphaned (Android cleans them up eventually)
- Nothing remains on any server

---

*Your data. Your device. Your control.*
