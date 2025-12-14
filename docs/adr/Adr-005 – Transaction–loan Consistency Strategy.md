# ADR-005: Transaction–Loan Consistency Strategy (Synchronous vs Event-driven)

## Status

Accepted

## Context

The system includes tightly related financial domains:

* **Transactions** (expenses, income, transfers)
* **Loans** (bank loans with principal, interest, remaining balance)

A key use case:

* When a **loan repayment transaction** is created, the loan's remaining principal must be reduced accordingly
* Interest and principal portions must be handled correctly

The architectural question:

* Should loan updates happen **synchronously** within the same request?
* Or **asynchronously** via domain events?

## Decision

The system will use a **synchronous consistency model** between Transaction and Loan domains.

Specifically:

* Loan repayment is treated as a **first-class domain use case**
* A single application service coordinates:

  * LoanPayment creation
  * Transaction creation
  * Loan balance update
* All operations execute within **one database transaction**

## Rationale

This decision is driven by domain characteristics:

* Financial data requires **strong consistency**
* Partial success is unacceptable (no “eventual money”)
* Business rules for loan repayment are deterministic and immediate

Using synchronous orchestration:

* Guarantees atomicity (all-or-nothing)
* Simplifies reasoning and debugging
* Avoids complex failure recovery logic
* Matches early-stage system complexity and scale

Event-driven consistency is intentionally avoided at this stage.

## Alternatives Considered

### Event-driven (Asynchronous) Consistency

**Pros**:

* Loose coupling between domains
* High scalability

**Cons**:

* Eventual consistency not acceptable for money
* Complex retry and compensation logic
* Harder to debug and reason about
* Overkill for current scale

### Synchronous Consistency (Chosen)

**Pros**:

* Strong consistency guarantees
* Simple and predictable behavior
* Clear transaction boundaries

**Cons**:

* Tighter coupling between domains
* Requires careful application service design

## Consequences

### Positive

* Financial correctness is guaranteed
* Easier testing and validation
* Clear domain invariants

### Negative

* Harder to split domains into independent services later

## Future Evolution

This decision may be revisited when:

* The system scales significantly
* Loan and Transaction domains need independent deployment
* Event sourcing or financial ledger systems are introduced

A future hybrid model may use:

* Synchronous command
* Asynchronous projection or reporting events

## Notes

This design intentionally favors **correctness and clarity over theoretical scalability**.
