# Security Policy

## 🔐 Supported Versions

We provide security updates for the latest stable release of jPOS (tip). Older versions (tail) may receive updates on a case-by-case basis (see [tip and tail](https://jpos.org/blog/2024/12/jpos-tip-and-tail)).

| Version | Supported          |
| ------- | ------------------ |
| 3.x.x   | :white_check_mark: |
| 2.x.x   | :white_check_mark: |
| < 2.0   | :x:                |

## 🛡️ Reporting Vulnerabilities

If you think you've found a vulnerability, please use the _Report a vulnerability_ button found in the [security tab](https://github.com/jpos/jPOS/security) of the project on Github or contact security at jpos dot org. Avoid disclosing security related issues publicly in GitHub Issues or pull requests.

We acknowledge reports within 24 hours (usually less) and follow coordinated disclosure practices.

This process is documented in GitHub's _Secure Coding_ guide: [Privately reporting a security vulnerability](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability#privately-reporting-a-security-vulnerability).

## 🔄 Patch Management

- Security vulnerabilities are triaged and patched promptly.
- Changelogs and CVEs (when applicable) document known issues.
- Releases are signed with GPG and validated for integrity.

## 🧪 Secure Development Practices

- All pull requests require approval by a core maintainer.
- Contributor License Agreements (CLAs and CCLAs when appropriate) are mandatory for significant contributions.
- Maintainers must enable GitHub 2FA and use signed commits.
- CI pipelines validate code and enforce reproducible builds and over 3000 unit tests.

## 📦 Dependency Management

- We review dependencies for known vulnerabilities using tools like OWASP dependency-check, that is integrated as a Gradle task.
- We avoid dependencies that fetch remote resources at build time.

## 📚 Additional Resources

- [Governance Policy](./GOVERNANCE.md)
- [Contributor Guidelines](./CONTRIBUTING.md)
- [ChangeLog](https://github.com/jpos/jPOS/wiki/ChangeLog)
- [Resources](https://jpos.org/resources/)

## Reporting a Vulnerability

If you think you've found a vulnerability, please use the _Report a vulnerability_ button found in the [security tab](https://github.com/jpos/jPOS/security) of the project on Github or contact security at jpos dot org.

This process is documented in GitHub's _Secure Coding_ guide: [Privately reporting a security vulnerability](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability#privately-reporting-a-security-vulnerability).



