# Security Agent

## Purpose
Secure all Jobzy API endpoints using Spring Security.

## Key Rules
- **Never** log sensitive data (passwords, tokens)
- **Always** use HTTPS in production (Azure handles this)
- **Validate** all input (see `backend-api-agent.md`)
- **Use** UUIDs not sequential IDs
- **Store** secrets in Azure Key Vault, not in code

## Common Vulnerabilities to Prevent
- ✅ SQL Injection: Use JPA/parameterized queries
- ✅ XSS: Validate/sanitize all input
- ✅ CSRF: Disabled for stateless JWT APIs
- ✅ Exposed secrets: Use environment variables