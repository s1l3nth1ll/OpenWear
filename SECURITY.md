# OpenWear Security Policy
 
## Security Philosophy
 
OpenWear handles potentially sensitive wearable and health-related information.
 
Security is therefore a fundamental part of the project.
 
The project aims to minimize attack surfaces, reduce unnecessary data collection, and keep sensitive information under the user's control.
 
---
 
## Supported Versions
 
OpenWear is currently under active development.
 
During the pre-alpha phase, security support will generally focus on the latest development version.
 
| Version | Supported |
| ------- | --------- |
| Development / main | ✅ |
| Older releases | ❌ |
 
This policy will be updated when stable releases are introduced.
 
---
 
## Reporting a Vulnerability
 
**Please do not publicly disclose security vulnerabilities before they have been investigated.**
 
If you discover a security vulnerability, please report it privately to the project maintainers.
 
A private security reporting mechanism will be established before the first public release.
 
Until then, please avoid publishing sensitive vulnerability details in public GitHub issues.
 
---
 
## What to Include
 
When reporting a vulnerability, please include as much of the following information as possible:
 
- Description of the vulnerability
- Affected component
- Affected version or commit
- Steps to reproduce
- Expected behavior
- Actual behavior
- Potential impact
- Proof of concept, if available
- Suggested mitigation, if known
 
Please do not include real users' personal or health data in vulnerability reports.
 
Use synthetic or anonymized data whenever possible.
 
---
 
## Security Response
 
The maintainers will attempt to:
 
1. Acknowledge the report
2. Reproduce the issue
3. Determine its severity and impact
4. Develop a fix or mitigation
5. Test the fix
6. Release the fix when appropriate
7. Publish relevant security information
 
Response times may vary while the project is in early development.
 
---
 
## Sensitive Data
 
Never include the following in public issues or pull requests:
 
- Personal health information
- Real wearable exports containing personal information
- Bluetooth credentials
- Authentication tokens
- Private keys
- Passwords
- API keys
- Personally identifying information
 
Remove or anonymize sensitive information before submitting diagnostic data.
 
---
 
## Responsible Disclosure
 
OpenWear encourages responsible security research.
 
Security researchers who report vulnerabilities privately and responsibly will be credited when appropriate and with their permission.
 
---
 
## Scope
 
The security policy applies primarily to the OpenWear project itself.
 
Third-party devices, operating systems, manufacturers, firmware, and external services may have their own security policies and vulnerabilities.
 
OpenWear cannot guarantee the security of third-party systems.
 
---
 
## Experimental Software
 
OpenWear is currently experimental software.
 
Users should understand that early versions may contain security vulnerabilities and should not be relied upon for critical or medical applications.
