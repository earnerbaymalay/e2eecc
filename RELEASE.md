# Release Process

## Versioning

Cypherchat uses [Semantic Versioning](https://semver.org/): `MAJOR.MINOR.PATCH`

- **MAJOR:** Breaking changes (e.g., protocol changes, key format changes)
- **MINOR:** New features (e.g., new screens, new transport methods)
- **PATCH:** Bug fixes and security patches (e.g., crypto fixes, memory leaks)

## Release Checklist

### Before Release
- [ ] All tests passing (`./gradlew test`)
- [ ] Lint clean (`./gradlew lintDebug`)
- [ ] Security review completed for any crypto changes
- [ ] CHANGELOG.md updated with all changes since last release
- [ ] Documentation updated (README, USAGE, SECURITY, ARCHITECTURE)
- [ ] ProGuard rules validated for release build
- [ ] No debug logging in release build (`BuildConfig.DEBUG` is `false`)
- [ ] No test/mock data in production code
- [ ] Dependencies updated to latest stable versions
- [ ] `versionCode` and `versionName` bumped in `app/build.gradle.kts`

### Release Build
```bash
# Build release APK
./gradlew assembleRelease

# Build release AAB (for Play Store / distribution)
./gradlew bundleRelease

# Verify signing
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

### Post-Release
- [ ] Create GitHub Release with tag (e.g., `v1.0.0`)
- [ ] Write release notes in CHANGELOG.md
- [ ] Publish release on GitHub with release notes
- [ ] Update ROADMAP.md with completed items
- [ ] Announce in GitHub Discussions

## Release Branch Strategy

- `main` — stable, release-ready code
- `develop` — ongoing development (if adopted)
- `release/vX.Y.Z` — release candidate branches
- `hotfix/*` — urgent security or critical bug fixes

## Hotfix Process

For critical security vulnerabilities:

1. Create branch from latest release tag: `git checkout -b hotfix/security-fix v1.0.0`
2. Fix the vulnerability
3. Add regression test
4. Release as `PATCH` version bump
5. Coordinate public disclosure timing with security reporter (if applicable)

## Distribution

| Channel | Format | Audience |
|---|---|---|
| GitHub Releases | APK | Developers, early adopters |
| F-Droid (future) | AAB/F-Droid repo | Privacy-conscious users |
| Play Store (future) | AAB | General users |

## Signing

Release builds are signed with a release keystore. The keystore is **NOT** committed to the repository and is managed securely by the release manager.

---

*Every release is a commitment to our users' security.*
