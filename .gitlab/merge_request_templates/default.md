## Merge Request

### What does this MR do?
<!-- Brief description of changes -->

### Related Issues
<!-- Link to related issues: Closes #123, Relates to #456 -->

### Changes Made
- 
- 
- 

### Security Checklist
- [ ] No sensitive data logged to Logcat
- [ ] No hardcoded secrets/keys
- [ ] Used `SecureRandom` (not `Math.random()`)
- [ ] Sensitive variables cleared from memory after use
- [ ] Android Keystore used for root keys (not EncryptedSharedPreferences)
- [ ] No new dependencies added (or approved by maintainer)
- [ ] SQLCipher queries parameterized (no SQL injection)

### Code Quality Checklist
- [ ] Follows MVVM + Clean Architecture
- [ ] No "God" Activities/ViewModels
- [ ] Error handling implemented (no empty `catch` blocks)
- [ ] Unit tests added/updated
- [ ] `@Preview` composables included for UI changes
- [ ] Lint warnings addressed
- [ ] Code formatted (4 spaces, 120 char line limit)

### Testing
<!-- How was this tested? -->
- [ ] Unit tests pass
- [ ] Manual testing on device
- [ ] Tested on Android API levels: <!-- e.g., 26, 30, 34 -->

### Screenshots/Videos
<!-- If UI changes, attach before/after screenshots -->

### Breaking Changes
<!-- Does this MR break existing functionality? -->
- [ ] No breaking changes
- [ ] Breaking changes (describe below)

### Deployment Notes
<!-- Any migration steps, database changes, or config updates? -->

---
**Assignee**: @<!-- mention reviewer -->  
**Labels**: `needs-review`

