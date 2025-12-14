# ADR-007: Ledger-style Transaction Model vs CRUD Balance

## Status

Accepted

## Context

The system manages financial data including:

* Account balances
* Expense and income transactions
* Loan repayments and outstanding principal

Two common modeling approaches exist:

* **CRUD Balance Model**: store and update current balances directly
* **Ledger-style Model**: store immutable transactions and derive balances

The choice affects:

* Data integrity
* Auditability
* Error recovery
* Long-term correctness

## Decision

The system will adopt a **Ledger-style transaction model** as the source of truth.

Specifically:

* Transactions are immutable once created
* Balances are derived from transactions
* Optional balance snapshots may be stored for performance

## Rationale

Financial systems demand:

* Traceability
* Auditability
* Deterministic correctness

Ledger-style modeling:

* Preserves a complete history of financial events
* Makes errors visible rather than overwritten
* Enables auditing and reconciliation
* Aligns with real-world accounting principles

Directly updating balances via CRUD risks:

* Silent data corruption
* Loss of historical context
* Difficult debugging and auditing

## Alternatives Considered

### CRUD Balance Model

**Pros**:

* Simple implementation
* Fast reads

**Cons**:

* Error-prone updates
* No audit trail
* Hard to correct historical mistakes

### Ledger-style Model (Chosen)

**Pros**:

* Immutable audit log
* Strong data integrity
* Easier financial reasoning

**Cons**:

* More complex queries
* Requires aggregation for balances

## Consequences

### Positive

* Full financial traceability
* Easier reconciliation and debugging
* Safer evolution of business rules

### Negative

* Additional complexity in reporting
* Performance considerations for large datasets

## Performance Strategy

* Maintain derived balance tables or materialized views
* Recompute balances from ledger when needed
* Treat derived balances as cache, not source of truth

## Notes

This decision prioritizes **financial correctness over convenience**.

In finance, history is a feature—not a cost.
