# ADR-004: Internal JWT vs Session-based Authentication

## Status

Accepted

## Context

The system uses Google OIDC as an external Identity Provider (IdP) for user authentication (see ADR-003). After successful authentication, the backend must manage authenticated user sessions for subsequent API calls.

Two primary approaches are considered:

* **Session-based authentication** (server-side session storage)
* **Internal JWT-based authentication** (stateless tokens issued by backend)

The system is expected to:

* Support web and potentially mobile clients
* Scale horizontally
* Remain simple to operate in early stages
* Be compatible with future service decomposition

## Decision

The system will use **Internal JWT-based authentication** issued by the backend after successful Google OIDC login.

Flow summary:

1. User authenticates via Google OIDC
2. Backend validates Google ID Token
3. Backend issues its own short-lived JWT (Internal Access Token)
4. Clients include JWT in `Authorization: Bearer` header for all subsequent requests

No server-side session state will be maintained.

## Rationale

Internal JWT-based authentication is chosen because:

* **Stateless by design**, enabling horizontal scaling without shared session storage
* **Clear trust boundary**: Google handles identity proof, backend controls authorization
* **Client-agnostic**, suitable for SPA, mobile, and API clients
* **Future-proof** for potential microservice extraction
* **Operational simplicity** compared to session replication or sticky sessions

The backend-issued JWT is intentionally decoupled from Google ID tokens to:

* Avoid tight coupling to external token lifecycles
* Allow internal role and permission modeling
* Enable future identity provider changes

## Alternatives Considered

### Session-based Authentication

**Pros**:

* Simple mental model
* Easy token revocation

**Cons**:

* Requires session store (Redis, DB)
* Adds operational complexity
* Complicates horizontal scaling
* Poor fit for API-first and mobile clients

### Reusing Google ID Token Directly

**Pros**:

* Fewer moving parts

**Cons**:

* Token lifecycle controlled externally
* Limited authorization flexibility
* Leaks identity provider concerns into domain logic

## Consequences

### Positive

* Stateless backend authentication
* Clean separation of authentication vs authorization
* Easier future migration to distributed architecture

### Negative

* Token revocation requires expiration or blacklist strategy
* Slightly more complex initial implementation

## Notes

* JWTs should be short-lived (e.g., 15–30 minutes)
* Refresh token strategy may be added later if needed
* Authorization logic remains fully server-controlled
