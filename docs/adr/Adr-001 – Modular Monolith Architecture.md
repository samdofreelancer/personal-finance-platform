# ADR-001: Use Modular Monolith Architecture

## Status

Accepted

---

## Context

The Personal Finance & Loan Management System is designed to manage:

* Financial accounts and balances
* Financial transactions (income, expense, transfer)
* Personal expenses
* Bank loans with declining balance interest calculation

The system contains **financially critical workflows**, especially:

* Creating transactions that affect account balances
* Processing loan repayments that must update both:

  * Account balance
  * Outstanding loan principal

These workflows require **strong transactional consistency** and clear ownership of business rules.

At the early stage of the project:

* The system is developed by a small team
* The expected user scale is moderate
* Operational simplicity and development speed are prioritized

A key architectural decision was required:

* Should the system start with a **Microservices Architecture** or a **Modular Monolith**?

---

## Decision

We decided to implement the system as a **Modular Monolith**.

The application will:

* Be deployed as a single runtime unit
* Use a single relational database
* Be internally structured into well-defined modules aligned with bounded contexts
* Enforce module boundaries through package structure and architectural rules

The architecture is intentionally designed to allow **future extraction of modules into microservices** if scaling or organizational needs arise.

---

## Alternatives Considered

### Option 1: Microservices Architecture

**Pros**

* Independent deployment of services
* Independent scaling per service
* Technology flexibility across services

**Cons**

* Distributed transactions required for financial workflows
* Increased operational complexity (service discovery, monitoring, deployment)
* Higher cognitive load for a small team
* Risk of premature optimization
* Increased difficulty in ensuring financial consistency

---

### Option 2: Modular Monolith (Chosen)

**Pros**

* Strong transactional consistency using a single database transaction
* Simplified deployment and operations
* Faster development velocity
* Clear domain boundaries within a single codebase
* Easier reasoning about financial invariants

**Cons**

* Limited independent scaling of individual modules
* Requires discipline to prevent tight coupling between modules

---

## Consequences

### Positive Consequences

* All financial operations (transactions, loan repayments) are executed atomically
* Business rules remain centralized and consistent
* Reduced infrastructure and operational overhead
* Faster feedback loop during development

### Negative Consequences

* The entire application must be deployed together
* Scaling is primarily vertical in early stages
* Architectural discipline is required to avoid a "big ball of mud"

---

## Enforcement & Guidelines

To ensure the success of the Modular Monolith approach:

* Each module must own its domain logic and data access
* Cross-module communication must occur through explicit application services
* Direct database access across module boundaries is prohibited
* Financial invariants must be enforced at the domain layer

---

## When to Revisit This Decision

This decision should be revisited if one or more of the following conditions occur:

* Significant growth in user base requiring independent scaling
* Multiple teams working concurrently on different bounded contexts
* Clear operational or organizational benefits from service separation

---

## Notes

This decision does not reject microservices as an architectural style.
It intentionally delays their adoption until there is a **clear and measurable need**.

---

**End of ADR-001**
