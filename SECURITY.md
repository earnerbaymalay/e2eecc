# 🛡️ Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| 1.0.0-alpha | ✅ Yes |
| Pre-alpha | ❌ No |

## Reporting a Vulnerability

**If you find a security vulnerability, please do NOT open a public issue.** Instead:

1. **Email:** [security@cypherchat.com](mailto:security@cypherchat.com) *(placeholder — update when real email is set up)*
2. **Include:**
   - A description of the vulnerability
   - Steps to reproduce
   - Potential impact assessment
   - Suggested fix (if you have one)

## Response Timeline

- **Acknowledgment:** Within 48 hours
- **Initial assessment:** Within 7 days
- **Fix or mitigation:** Within 30 days (critical: 7 days)
- **Public disclosure:** Coordinated with reporter, after fix is deployed

## What We Consider a Vulnerability

- Cryptographic weaknesses (e.g., predictable IVs, weak key derivation)
- Key extraction possibilities (e.g., keys left in memory, Keystore bypass)
- Information leakage (e.g., plaintext written to disk, logs in release builds)
- Authentication bypasses
- Denial of service vectors in the transport layer

## What Is NOT a Vulnerability

- Issues with alpha-stage UI placeholders
- UX bugs that don't leak data (e.g., wrong color, misaligned text)
- Feature requests (please use GitHub Issues)
- Vulnerabilities in dependencies that we've already addressed

## Security Design Principles

Cypherchat is designed with the following principles:

1. **Zero-knowledge by default** — the app assumes nothing and trusts no server
2. **Defense in depth** — multiple layers of encryption (Double Ratchet + SQLCipher + Keystore)
3. **Memory hygiene** — plaintext and keys are zeroed after use
4. **Hardware-backed security** — keys are generated and stored in Android Keystore TEE
5. **Open for audit** — every line of code is public

## Bug Bounty

We don't have a formal bug bounty program, but **serious security findings will be credited** in our security advisory process and release notes.

---

*Thank you for keeping Cypherchat secure.*
