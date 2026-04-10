<div align="center">

# 🛡️ C Y P H E R C H A T
### *Troubleshooting & Support.*

**[📖 Usage Guide](docs/USAGE.md)** · **[📲 Sideload Hub](https://earnerbaymalay.github.io/sideload/)** · **[↩ Back to README](README.md)**

</div>

---

## Installation & Build Issues

### APK won't install
- Enable "Install unknown apps" for your browser or file manager in Android settings.
- If installation fails, clear package installer cache and retry.
- Ensure your device meets minimum requirements: Android 7.0+ (API 24), 100 MB free storage.

### Build fails from source
- Verify JDK 17 is installed and configured in Android Studio (`File → Settings → Build → Gradle → JDK`).
- Ensure Android SDK 24-34 is installed via SDK Manager.
- Run `./gradlew clean` then rebuild.
- Check that Gradle sync completes without errors.
- For detailed build commands, see [README.md → Build and Run](README.md#build-and-run).

### App crashes on launch
- Check `logcat` for specific errors: `adb logcat | grep -i cypherchat`
- Ensure Android Keystore is available and functional (some custom ROMs may have issues).

---

## Messaging Issues

### Cannot create invitation link
- Network connectivity is required for SimpleX relay connections. Verify your internet connection.
- Check that network permissions are granted in Android settings.

### Messages not sending
- This is a known alpha limitation. The network layer is not fully integrated yet and will show a placeholder error.
- Refer to the [roadmap](docs/ROADMAP.md) for updates on SimpleX SDK integration.

---

## Data & Security Issues

### Database corruption
- Reinstallation is required.
- **Warning:** Your database is tied to your device's Keystore and cannot be migrated. Reinstallation will result in data loss.

### Key generation fails
- Ensure Android Keystore system is functional.
- Try clearing app data and reopening the app to regenerate keys.

---

## Need More Help?

- **[📖 Read the Usage Guide](docs/USAGE.md)** for detailed instructions on installation, starting conversations, and security best practices.
- **[📲 Visit Sideload Hub](https://earnerbaymalay.github.io/sideload/)** for alternative installation methods and iPhone PWA access.
- **[🐛 Report a Bug](https://github.com/earnerbaymalay/e2eecc/issues)** on GitHub with device details, Android version, logcat output, and steps to reproduce.

---

[MIT License](LICENSE)
