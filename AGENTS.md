# AGENTS.md

Guidance for coding assistants, and for humans using coding assistants, when contributing to jPOS.

This document applies to AI-assisted contributions. It complements the normal jPOS contribution process. It does not replace human review, project governance, or maintainer judgment.

## Policy

### 1. Standard project rules apply

AI-assisted contributions must follow the same rules as any other contribution.

Read and follow:

- `README.md`
- `CONTRIBUTING.md`
- `GOVERNANCE.md`
- `SECURITY.md`
- `MAINTAINERS.md`
- relevant files under `legal/`

Use of AI does not relax project requirements for review, testing, licensing, provenance, or security.

### 2. Responsibility remains with the human submitter

A coding assistant may help draft, refactor, analyze, or review a change. The human submitter remains responsible for:

- correctness
- security impact
- provenance
- license compliance
- testing
- final submission

If you submit an AI-assisted patch, you are responsible for it.

### 3. Signed-off-by is human-only

Coding assistants must **not** add `Signed-off-by` lines.

Only a human may certify the Developer Certificate of Origin and accept responsibility for the contribution.

### 4. CLA and CCLA requirements still apply

AI-assisted contributions are subject to the same legal requirements as any other contribution.

For non-trivial changes:

- individual contributors must have a valid CLA
- corporate contributors must have a valid CCLA where applicable

These requirements must be satisfied by humans or authorized legal entities, never by an AI system.

See:

- `GOVERNANCE.md`
- `legal/cla-template.txt`
- `legal/ccla-template.txt`

### 5. License and provenance are mandatory

jPOS is licensed under the GNU Affero General Public License version 3.

Coding assistants must not introduce material with unclear, unverified, or incompatible provenance or licensing.

Do not copy from:

- incompatible open source projects
- blog posts or tutorials
- Q&A sites
- vendor documentation
- generated output of unknown provenance

If third-party material is reused, a human must verify license compatibility and attribution before submission.

### 6. Be transparent about meaningful AI assistance

Meaningful AI assistance should be disclosed in the commit message, pull request description, or both.

Recommended format:

`Assisted-by: TOOL_OR_AGENT:MODEL`

Example:

`Assisted-by: OpenClaw:gpt-5.4`

This is for transparency. It does not transfer authorship or responsibility.

## Practical rules

### 1. Preserve the character of jPOS

jPOS is a conservative, production-oriented codebase used in security-sensitive financial systems.

Prefer:

- small, focused changes
- explicit code
- stable behavior
- minimal diffs
- maintainable solutions

Avoid gratuitous dependencies, framework churn, speculative rewrites, and fashion-driven abstractions.

Do not add a new library when the JDK or existing jPOS code already solves the problem.

### 2. Be conservative in security-sensitive code

Use extra care in:

- serialization and deserialization
- cryptography and key management
- HSM integration
- protocol handling
- transaction processing
- persistence and recovery logic
- authentication and secrets handling
- build, release, and dependency changes

In these areas, prefer narrow changes, explicit reasoning, and straightforward review.

Do not silently weaken checks, validation, or security controls.

### 3. Keep patches reviewable

Do not mix unrelated refactors with functional fixes.

When behavior changes, update tests and documentation where appropriate.

If no test is added, say why.

Do not claim something was tested unless it actually was.

### 4. State what was verified

When preparing a patch or PR, say clearly:

- what was tested
- what was not tested
- any environment limitations
- any known pre-existing failures encountered

Do not invent test results, benchmarks, issue references, or security claims.

### 5. Do not overclaim

If something is uncertain, inferred, or only partially verified, say so.

Do not present generated output as authoritative unless a human has checked it.

## Final note

Coding assistants can help produce patches. They do not become legal contributors, do not sign agreements, do not certify origin, and do not assume responsibility.

The human submitter remains responsible for the final contribution.
