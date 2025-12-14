# Sequence Diagram – Loan Repayment

This sequence describes the **loan repayment flow**, covering both **principal and interest** handling.

---

## Scenario

User repays a bank loan. The repayment includes:

* Interest portion
* Principal portion

The remaining loan balance must be reduced accordingly.

---

## Sequence (Textual Diagram)

```
User
  |
  | Repay Loan (amount)
  v
Web Frontend
  |
  | POST /loans/{id}/repay
  v
LoanController
  |
  v
LoanApplicationService
  |
  | Calculate repayment
  v
LoanPaymentService
  |
  | principal, interest
  v
TransactionApplicationService
  |
  | Create Transaction (Ledger)
  v
TransactionRepository
  |
  v
LoanRepository
  |
  | Update remaining principal
  v
Database (Atomic Commit)
```

---

## Step-by-Step Description

1. User submits a loan repayment request
2. LoanApplicationService loads loan state
3. LoanPaymentService calculates interest and principal portions
4. TransactionApplicationService records an immutable transaction
5. Loan remaining principal is reduced
6. All operations commit in a single database transaction

---

## Guarantees

* Atomicity: no partial updates
* Strong consistency
* Ledger-first correctness

---

**End of Sequence Diagram**
