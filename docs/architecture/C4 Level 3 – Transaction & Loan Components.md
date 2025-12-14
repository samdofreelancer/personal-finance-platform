# C4 Level 3 – Component Diagram: Transaction & Loan Domains

This document describes **C4 Level 3 (Component Diagrams)** for the core financial domains: **Transaction** and **Loan**.

---

## Transaction Domain – Component Diagram

### Responsibility

The Transaction domain acts as the **financial ledger** and the single source of truth for all monetary movements.

### Components

* **TransactionController**

  * API entry point
  * Validates requests and authentication context

* **TransactionApplicationService**

  * Orchestrates transaction use cases
  * Ensures business rules are applied

* **TransactionFactory**

  * Creates immutable Transaction entities
  * Validates transaction invariants

* **TransactionRepository**

  * Persists and retrieves transactions

* **TransactionEntity**

  * Immutable ledger record
  * Represents a financial event

### Component Interaction (Textual)

```
TransactionController
        |
        v
TransactionApplicationService
        |
        v
TransactionFactory --> TransactionEntity
        |
        v
TransactionRepository
```

---

## Loan Domain – Component Diagram

### Responsibility

The Loan domain manages:

* Loan lifecycle
* Outstanding principal
* Repayment rules

It does **not** manipulate money directly.

### Components

* **LoanController**

  * API entry point for loan operations

* **LoanApplicationService**

  * Coordinates loan use cases
  * Delegates financial recording to Transaction domain

* **LoanPaymentService**

  * Calculates principal vs interest
  * Applies repayment rules

* **LoanRepository**

  * Persists loan state

* **LoanEntity**

  * Represents loan contract and remaining balance

### Component Interaction (Textual)

```
LoanController
      |
      v
LoanApplicationService
      |
      v
LoanPaymentService
      |
      +--> TransactionApplicationService
      |
      v
LoanRepository --> LoanEntity
```

---

## Key Design Rules

* Transaction domain is **money-authoritative**
* Loan domain never updates balances directly
* Cross-domain interaction happens only via application services

---

**End of C4 Level 3 Document**
