# ADR-003: Use Google OIDC over Custom Identity Management

## Status

Accepted

---

## Context

The Personal Finance & Loan Management System requires user authentication to:

* Protect sensitive financial data
* Associate all accounts, transactions, and loans with a specific user
* Enforce access control at the API level

At an early stage, the system must support:

* Secure authentication
* Minimal operational overhead
* Fast onboarding for end users

A key decision was required:

* Should the system implement a **custom identity management solution** (username/password, user store, password reset, etc.)?
* Or should it rely on an **external identity provider** using industry standards?

---

## Decision

We decided to use **Google OpenID Connect (OIDC)** as the primary authentication mechanism instead of building a custom identity system.

The system will:

* Delegate user authentication to Google via OIDC
* Validate Google-issued ID tokens
* Issue an internal JWT for authenticated API access

The backend remains the **authorization authority** for application-specific access control.

---

## Rationale

Building and maintaining a secure identity system requires:

* Password storage and hashing
* Account recovery and reset flows
* Protection against common attack vectors
* Ongoing security maintenance

By using Google OIDC:

* Authentication security is outsourced to a trusted provider
* The system avoids handling user passwords entirely
* Users benefit from a familiar and frictionless login experience

This aligns with the project goal of prioritizing **business logic and financial correctness** over commodity infrastructure.

---

## Authentication Flow Overview

1. The user initiates login from the web frontend
2. The frontend redirects the user to Google Authentication
3. Google authenticates the user and returns an ID token
4. The backend validates the ID token
5. The backend issues an internal JWT
6. The internal JWT is used for all subsequent API calls

---

## Alternatives Considered

### Option 1: Custom Identity Management

**Pros**

* Full control over user accounts
* No dependency on third-party providers

**Cons**

* High implementation complexity
* Significant security responsibility
* Increased development and maintenance effort
* High risk of security vulnerabilities

---

### Option 2: Google OIDC (Chosen)

**Pros**

* Industry-standard authentication (OIDC)
* Strong security guarantees
* No password handling in the system
* Reduced development and operational overhead
* Faster user onboarding

**Cons**

* Dependency on an external provider
* Requires internet connectivity to authenticate

---

## Consequences

### Positive Consequences

* Reduced security risk
* Faster time-to-market
* Simplified authentication flows
* Lower operational burden

### Negative Consequences

* Users must have a Google account
* Authentication availability depends on Google services

---

## Enforcement & Guidelines

* The system must never store user passwords
* All authentication must go through OIDC
* Internal JWTs must have expiration and be signed securely
* Authorization decisions remain within the backend application

---

## When to Revisit This Decision

This decision may be revisited if:

* Support for non-Google identity providers is required
* Regulatory requirements mandate in-house identity management
* Enterprise SSO integration becomes necessary

---

## Notes

This decision intentionally treats identity management as a commodity and focuses development effort on core financial domains.

---

**End of ADR-003**
