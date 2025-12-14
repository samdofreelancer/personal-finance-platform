# ADR-006: Database per Domain vs Shared Database

## Status

Accepted

## Context

The system is designed as a **modular monolith** with clearly separated domains:

* Identity / User
* Accounts
* Transactions
* Loans
* Expense Management

A common architectural decision arises:

* Should each domain own its **own database** (Database-per-Domain)?
* Or should all domains share **a single database** with logical separation?

This decision strongly affects:

* Transaction management
* Operational complexity
* Future service extraction

## Decision

The system will use a **Shared Database** with **strict domain ownership at the schema level**.

Specifically:

* One physical database instance
* Separate schemas or table namespaces per domain
* No cross-domain foreign keys enforced at database level
* Domain boundaries enforced in application layer

## Rationale

Database-per-Domain is attractive for microservices, but at this stage:

* The system runs as a single deployment unit
* Strong consistency across domains is required (see ADR-005)
* Distributed transactions are explicitly avoided

A shared database:

* Enables **atomic transactions** across domains
* Simplifies deployment and operations
* Reduces infrastructure and operational cost
* Fits the modular monolith approach

Logical separation preserves conceptual boundaries without premature distribution.

## Alternatives Considered

### Database per Domain

**Pros**:

* Strong domain isolation
* Easier service extraction later

**Cons**:

* No atomic cross-domain transactions
* Requires eventual consistency or saga patterns
* Operational overhead
* Over-engineering for current scale

### Shared Database (Chosen)

**Pros**:

* Strong consistency guarantees
* Simpler transaction management
* Lower operational complexity

**Cons**:

* Requires discipline to avoid schema coupling
* Future extraction requires migration effort

## Consequences

### Positive

* Financial invariants can be enforced atomically
* Easier debugging and data inspection
* Faster development velocity

### Negative

* Harder to split domains into independent databases later

## Enforcement Rules

* Domains may not directly query or modify other domains’ tables
* Cross-domain interaction must go through application services
* No foreign keys between domain schemas

## Future Evolution

When domains require independent scaling or deployment:

* Schemas can be extracted gradually
* Database-per-Domain can be introduced intentionally

This decision optimizes for **correctness and simplicity today**, not theoretical scalability.
