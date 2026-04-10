<div align="center">

# ⬡ C Y P H E R C H A T
### *Troubleshooting & Support.*

</div>

---

## Installation and build issues

### App crashes on launch
- Check `logcat` for specific errors.
- Ensure Android Keystore is available and functional (some custom ROMs may have issues).

### Build fails
- Verify correct JDK version (JDK 17) and Android SDK (24-34) are installed.
- Ensure Gradle is synced correctly in Android Studio.

---

## Messaging issues

### Cannot create invitation
- Network connectivity is required for SimpleX relay connections. Verify your internet connection.

### Messages not sending
- This is a known alpha limitation. The network layer is not fully integrated yet and will show a placeholder error.
- Refer to the roadmap for updates on SimpleX SDK integration.

---

## Data and security issues

### Database corruption
- Reinstallation is required.
- **Warning:** Your database is tied to your device's Keystore and cannot be migrated. Reinstallation will result in data loss.

---

## General issues

### App is slow
- Check device resources.
- Ensure no other heavy applications are running in the background.

---

[MIT License](LICENSE)
