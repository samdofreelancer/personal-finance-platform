# Software Requirements Specification (SRS)

## 1. Introduction

### 1.1 Purpose

This document specifies the software requirements for the **Personal Finance & Loan Management System** – a web-based system that enables users to manage:

* Bank loans
* Personal expenses
* Financial accounts
* Financial transactions

The document is intended to:

* Facilitate discussion among stakeholders
* Serve as a baseline for system architecture and design
* Guide development and testing activities

---

### 1.2 Scope

The system allows users to:

* Authenticate using Google Login
* Manage multiple personal financial accounts
* Record income and expense transactions
* Manage bank loans with **declining balance interest calculation**
* Record loan repayments (principal and interest) and automatically update outstanding loan balances

The system does **not** include:

* Real online payment processing
* Direct bank integrations
* Multi-user data sharing (Phase 1)

---

### 1.3 Definitions & Acronyms

* **Account**: A financial account owned by a user (cash, bank account, wallet, etc.)
* **Transaction**: A financial movement (income, expense, transfer)
* **Loan**: A bank loan
* **Loan Payment**: A single repayment event for a loan
* **Outstanding Principal**: Remaining unpaid loan principal
* **OIDC**: OpenID Connect

---

## 2. Overall Description

### 2.1 Product Perspective

The system is built as a **modular monolith** with a design that allows future evolution into microservices if required.

High-level modules include:

* Identity & Access
* Account Management
* Transaction Management
* Expense Management
* Loan Management
* Reporting

---

### 2.2 User Classes

* **End User**: An individual managing their own personal finances
* **Administrator (Future)**: System administrator

---

### 2.3 Operating Environment

* Web browsers (Chrome, Edge, Safari)
* Backend: Java 17 with Spring Boot
* Database: PostgreSQL

---

### 2.4 Design Constraints

* Monetary values must be handled with high precision
* All balance updates must be executed within database transactions
* Negative balances must be controlled and validated by business rules

---

## 3. System Architecture (High Level)

### 3.1 Architectural Style

* Modular Monolith
* Domain-Driven Design (DDD)
* Layered Architecture

### 3.2 Major Components

* **Authentication Module**

  * Google OIDC Login
  * Internal JWT issuance

* **Account Module**

  * Account lifecycle management
  * Balance management

* **Transaction Module**

  * Recording income, expense, and transfer transactions
  * Acts as the single source of truth for cash flow

* **Loan Module**

  * Loan lifecycle management
  * Declining balance interest calculation
  * Loan repayment processing

* **Reporting Module**

  * Expense summaries
  * Outstanding loan overview
  * Cash flow reporting

---

## 4. Functional Requirements

### 4.1 Authentication

* FR-01: Users shall be able to authenticate using Google Login
* FR-02: Upon successful authentication, the system shall issue an internal JWT
* FR-03: All secured APIs shall require a valid JWT

---

### 4.2 Account Management

* FR-10: Users shall be able to create financial accounts
* FR-11: An account shall include type, currency, and initial balance
* FR-12: Each account shall belong to exactly one user

---

### 4.3 Transaction Management

* FR-20: Users shall be able to create financial transactions
* FR-21: Each transaction shall belong to one account
* FR-22: Transaction types shall include income, expense, and transfer
* FR-23: Creating a transaction shall update the associated account balance

---

### 4.4 Expense Management

* FR-30: An expense shall be a specialized form of transaction
* FR-31: Expenses shall be categorized
* FR-32: Expenses shall be used for expense reporting and analytics

---

### 4.5 Loan Management

* FR-40: Users shall be able to create bank loans

* FR-41: A loan shall include:

  * Principal amount
  * Annual interest rate
  * Loan term (in months)
  * Start date

* FR-42: The system shall calculate repayment schedules using the declining balance method

---

### 4.6 Loan Repayment

* FR-50: Users shall be able to record loan repayments

* FR-51: A loan repayment shall consist of:

  * Principal payment amount
  * Interest payment amount

* FR-52: When a loan repayment is recorded, the system shall:

  * Create an expense transaction
  * Deduct funds from the selected account
  * Reduce the outstanding loan principal

* FR-53: The system shall prevent principal repayment amounts that exceed the current outstanding principal

---

## 5. Non-Functional Requirements

### 5.1 Performance

* NFR-01: Standard API responses shall complete within 300ms

### 5.2 Security

* NFR-10: JWTs shall have a defined expiration time
* NFR-11: Users shall only be able to access their own data

### 5.3 Reliability

* NFR-20: All balance and loan updates shall be atomic
* NFR-21: The system shall ensure data consistency in the event of failures

### 5.4 Maintainability

* NFR-30: The codebase shall follow Clean Architecture principles
* NFR-31: Business logic shall remain independent of framework-specific concerns

---

## 6. Assumptions & Future Enhancements

### 6.1 Assumptions

* One user represents one individual
* Single primary currency in Phase 1

### 6.2 Future Enhancements

* Multi-currency support
* Budget planning
* Automated loan repayment reminders
* Gradual migration to microservices

---

## 7. Appendix

### 7.1 Technology Stack

* Backend: Java 17, Spring Boot 3
* Database: PostgreSQL
* Authentication: Google OIDC
* Frontend: React with TypeScript

---

**End of Document**
