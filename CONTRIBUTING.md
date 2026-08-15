# Contributing to OpenWear
 
Thank you for your interest in OpenWear.
 
OpenWear is intended to become a community-driven open-source wearable platform.
 
Contributions are welcome.
 
---
 
## Ways to Contribute
 
You can contribute through:
 
- Android development
- BLE development
- Device drivers
- Protocol research
- Testing
- Documentation
- UI/UX
- Security research
- Privacy research
- Hardware development
- Firmware development
- Data format design
- Bug reports
- Feature requests
 
You do not need to be an expert to contribute.
 
---
 
## Before Contributing
 
Please read:
 
- [`README.md`](README.md)
- [`PRIVACY.md`](PRIVACY.md)
- [`SECURITY.md`](SECURITY.md)
 
If you are contributing code, please also check the existing architecture and documentation.
 
---
 
## Issues
 
Before opening an issue:
 
1. Search existing issues.
2. Check whether the problem exists in the latest version.
3. Provide enough information to reproduce the problem.
 
Useful information may include:
 
- Device
- Android version
- OpenWear version
- Steps to reproduce
- Expected behavior
- Actual behavior
- Relevant logs
 
Do not include private health information or credentials.
 
---
 
## Pull Requests
 
Pull requests should:
 
- Have a clear purpose
- Keep changes focused
- Include relevant documentation
- Avoid unnecessary dependencies
- Avoid collecting unnecessary user data
- Follow the project's coding conventions
- Include tests when practical
 
Large architectural changes should generally be discussed before implementation.
 
---
 
## Device Drivers
 
Device-specific code should remain isolated from the core application whenever possible.
 
A device driver should not introduce manufacturer-specific assumptions into the common data model unless necessary.
 
When adding device support, document:
 
- Device name
- Connection method
- Supported services
- Supported characteristics
- Available measurements
- Known limitations
- Required permissions
- Testing status
 
---
 
## Privacy Requirements
 
Contributions must respect OpenWear's privacy principles.
 
Do not introduce:
 
- Hidden telemetry
- Unnecessary tracking
- Advertising identifiers
- Mandatory accounts
- Unnecessary cloud dependencies
- Unnecessary collection of health data
 
Any feature requiring network communication should clearly document why it is necessary.
 
---
 
## Security
 
If you discover a security vulnerability, do not open a public GitHub issue.
 
Follow the process described in [`SECURITY.md`](SECURITY.md).
 
---
 
## Code Style
 
Follow the existing project's style and conventions.
 
For Android development, the project currently intends to use:
 
- Kotlin
- Jetpack Compose
- Android SDK conventions
- Gradle
 
Keep code readable and maintainable.
 
---
 
## Commit Messages
 
Use clear commit messages.
 
Examples:
 
```text
Add BLE device scanner
 
Implement Polar device discovery
 
Add local heart rate storage
 
Fix Bluetooth permission handling
