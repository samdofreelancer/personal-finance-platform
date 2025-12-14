# System Architecture Overview

This document provides a **System Diagram** using the **C4 Model – Level 1 (System Context)** and **Level 2 (Container Diagram)** for the Personal Finance & Loan Management System.

---

## C4 – Level 1: System Context Diagram

### Overview

The system is a **Personal Finance & Loan Management System** that allows individual users to:

* Manage personal accounts
* Track expenses and income
* Manage bank loans and repayments
* View balances and financial history

### External Actors & Systems

* **User**

  * Uses the web application to manage personal finances

* **Google Identity Platform**

  * Acts as the external Identity Provider (OIDC)
  * Handles authentication and identity verification

### System Context (Textual Diagram)

```
[ User ]
    |
    |  Login / Manage Finance
    v
[ Personal Finance & Loan Management System ]
    |
    |  OIDC Authentication
    v
[ Google OIDC ]
```

### Notes

* The system does not manage passwords
* All sensitive financial operations occur inside the system boundary
* Authentication is delegated, authorization is internal

---

## C4 – Level 2: Container Diagram

### Containers

#### 1. Web Frontend (SPA)

* Technology: React / Next.js (suggested)
* Responsibilities:

  * User interaction
  * Initiate Google login
  * Send authenticated API requests

#### 2. Backend Application (Modular Monolith)

* Technology: Java (Spring Boot) or similar
* Responsibilities:

  * Business logic
  * Authorization
  * Financial consistency
  * JWT issuance and validation

Subdomains inside backend:

* Identity (Auth Adapter)
* Account Management
* Transaction Ledger
* Loan Management
* Expense Management

#### 3. Database

* Technology: PostgreSQL
* Single physical database
* Schema separation per domain

#### 4. External Identity Provider

* Google OIDC

### Container Interaction Diagram (Textual)

```
[ User ]
    |
    | HTTPS
    v
[ Web Frontend (SPA) ]
    |
    | Google Login Redirect
    v
[ Google OIDC ]
    |
    | ID Token
    v
[ Web Frontend ]
    |
    | API Request + Google ID Token
    v
[ Backend Application ]
    |
    | Validate ID Token
    | Issue Internal JWT
    |
    | Business Operations
    v
[ PostgreSQL Database ]
```

---

## Architectural Characteristics

* **Monolithic deployment, modular design**
* **Strong consistency for financial operations**
* **Ledger-style transaction model**
* **Stateless authentication using JWT**

---

## Intentional Omissions

The following are intentionally excluded at this stage:

* Message broker
* Event streaming
* Distributed tracing
* Microservices

These may be introduced when system scale or organizational needs justify them.

---

## Summary

This architecture prioritizes:

* Financial correctness
* Simplicity
* Clear domain boundaries
* Evolutionary design

The system is designed to grow without prematurely paying the cost of distributed complexity.

---

**End of System Diagram Document**
