# Governance Policy for jPOS

jPOS is an open-source project governed by a meritocratic model, with contributions managed transparently and securely.

## 🧑‍💻 Maintainers

The list of current maintainers is available in the [MAINTAINERS](./MAINTAINERS.md) file. Maintainers are responsible for:
- Reviewing and approving pull requests.
- Managing security disclosures and releases.
- Ensuring compliance with licensing and contribution policies.

## ✅ Contributor Vetting

- All contributors of non-trivial changes must sign a Contributor License Agreement ([CLA](https://github.com/jpos/jPOS/blob/main/legal/cla-template.txt)).
- Corporate contributors must sign a Corporate CLA ([CCLA](https://github.com/jpos/jPOS/blob/main/legal/ccla-template.txt)).
- We require GitHub 2FA for maintainers and encourage signed commits.

## 🔍 Decision-Making

jPOS uses consensus among maintainers and community. Disagreements are resolved by the lead maintainer.

## 📚 Risk Management

- jPOS uses very few external dependencies by design, which significantly reduces its attack surface.
- We make a deliberate effort to monitor and participate in the communities of the dependencies we do use—this includes subscribing to relevant mailing lists, security advisories, and communication channels to stay informed about emerging risks.
- A lightweight risk register is maintained internally and updated with each major release.
- Known or emerging risks are tagged using GitHub Issues and, when applicable, reflected in our release notes.

## 🔐 Security and Supply Chain Integrity

- Releases are GPG-signed and published to Maven Central.
- CI workflows ensure reproducibility and deterministic builds.
- External dependencies are pinned and verified.

## 🔄 Governance Reviews

This policy is reviewed annually or after any major security incident.