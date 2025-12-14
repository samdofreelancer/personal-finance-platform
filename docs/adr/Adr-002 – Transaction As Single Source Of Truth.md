# ADR-002: Use Transaction as the Single Source of Truth for Monetary Flow

## Status

Accepted

---

## Context

The Personal Finance & Loan Management System manages multiple financial concepts, including:

* Accounts with balances
* Financial transactions (income, expense, transfer)
* Bank loans with outstanding principal
* Loan repayments consisting of principal and interest

A recurring architectural question arises:

* Where should monetary truth live?
* Should account balances or loan states be updated directly by multiple features?

Without a clear rule, the system risks:

* Balance inconsistencies
* Duplicate monetary logic across modules
* Difficult auditing and reconciliation

---

## Decision

We decided that **Transaction** is the **single source of truth for all monetary movements** in the system.

This means:

* All money inflow and outflow must be represented as a Transaction
* Account balances are derived from Transactions
* No feature is allowed to change an account balance without creating a Transaction

Loans do **not** manage money directly. They manage **outstanding principal**, which is adjusted based on validated repayment intent.

---

## Rationale

Financial systems require:

* Auditability
* Traceability
* Deterministic reconstruction of balances

By centralizing monetary flow in the Transaction domain:

* Every balance change is explainable
* Historical reconstruction is possible
* Business rules remain consistent

---

## Implications for Loan Repayment

When a loan repayment occurs:

1. The user specifies the repayment intent (principal + interest)
2. The system validates the repayment against loan state
3. A Transaction of type **expense** is created
4. The account balance is reduced based on the Transaction
5. The loan outstanding principal is reduced by the principal portion

The Transaction **does not know** about loans.
The Loan **does not manipulate money**.
Coordination is handled by an application service.

---

## Alternatives Considered

### Option 1: Account Balance as Source of Truth

**Pros**

* Simple mental model

**Cons**

* Poor auditability
* Difficult rollback and correction
* High risk of silent inconsistencies

---

### Option 2: Each Domain Manages Its Own Money

**Pros**

* Localized logic

**Cons**

* Duplicate monetary rules
* Inconsistent rounding and validation
* Extremely difficult reconciliation

---

## Consequences

### Positive Consequences

* Strong audit trail
* Clear ownership of monetary rules
* Simplified reporting and analytics
* Reduced risk of data inconsistency

### Negative Consequences

* Requires discipline to prevent shortcuts
* Slightly more steps for some use cases

---

## Enforcement Rules

* Account balance updates must only occur inside Transaction creation logic
* No domain other than Transaction may mutate account balances
* Application services must coordinate multi-domain workflows
* All monetary values must use precise decimal arithmetic

---

## When to Revisit This Decision

This decision may be revisited if:

* Event sourcing is adopted system-wide
* Regulatory or auditing requirements change significantly

---

## Notes

This decision aligns with financial system best practices and prioritizes correctness over convenience.

---

**End of ADR-002**
