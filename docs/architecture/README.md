# Architecture Overview – C4 Model (Level 1 & Level 2)

## 1. Overview

This document describes the high-level architecture of the **Personal Finance & Loan Management System** using the **C4 Model**, focusing on:

* **Level 1: System Context**
* **Level 2: Container Diagram**

The goal is to clearly communicate system boundaries, responsibilities, and major technical decisions without diving into implementation details.

---

## 2. C4 – Level 1: System Context Diagram

### 2.1 Primary Actor

**End User**

* An individual managing personal finances
* Uses the system to track accounts, expenses, transactions, and bank loans

---

### 2.2 External Systems

**Google Identity Platform**

* Provides authentication via OpenID Connect (OIDC)
* Used for user login only

---

### 2.3 System Under Design

**Personal Finance & Loan Management System**

* A web-based system that allows users to:

  * Manage financial accounts
  * Record financial transactions
  * Track personal expenses
  * Manage bank loans with declining balance interest

---

### 2.4 Context Relationships

* The **End User** interacts with the system through a web browser
* The system delegates authentication to **Google Identity Platform**
* After authentication, all business operations are handled internally by the system

---

### 2.5 Key Context-Level Decisions

* The system is **single-tenant per user** (data isolation by user ID)
* Authentication is outsourced to a trusted external identity provider
* Financial data consistency is handled entirely within system boundaries

---

## 3. C4 – Level 2: Container Diagram

### 3.1 Containers Overview

The system is implemented as a **modular monolith** consisting of multiple logical containers.

---

### 3.2 Containers Description

#### 3.2.1 Web Frontend

* **Type**: Single Page Application (SPA)
* **Technology**: React + TypeScript
* **Responsibilities**:

  * User interaction and UI rendering
  * Authentication redirection to Google
  * Displaying dashboards, reports, and forms
  * Calling backend APIs

---

#### 3.2.2 Backend Application

* **Type**: Modular Monolith
* **Technology**: Java 17, Spring Boot 3
* **Responsibilities**:

  * Enforcing business rules
  * Managing financial transactions
  * Coordinating loan repayments and account balance updates
  * Issuing internal JWTs after authentication

##### Internal Modules (Logical Containers)

* **Auth Module**

  * OIDC integration with Google
  * Internal JWT issuance and validation

* **Account Module**

  * Account lifecycle management
  * Balance updates

* **Transaction Module**

  * Recording income, expense, and transfer transactions
  * Acts as the single source of truth for cash flow

* **Expense Module**

  * Expense categorization
  * Expense analytics support

* **Loan Module**

  * Loan creation and management
  * Declining balance interest calculation
  * Loan repayment processing

* **Reporting Module**

  * Aggregated financial views
  * Read-only reporting queries

---

#### 3.2.3 Database

* **Type**: Relational Database
* **Technology**: PostgreSQL
* **Responsibilities**:

  * Persistent storage of all financial data
  * Enforcing transactional consistency (ACID)

---

### 3.3 Container Interactions

* The **Web Frontend** communicates with the **Backend Application** via RESTful APIs
* The **Backend Application** communicates with **PostgreSQL** using transactional queries
* Authentication flow:

  * Frontend redirects user to Google
  * Google returns ID token
  * Backend validates token and issues internal JWT

---

### 3.4 Key Container-Level Decisions

* **Modular Monolith over Microservices**

  * Strong transactional consistency for financial operations
  * Lower operational complexity
  * Clear migration path to microservices if needed

* **Database as Single Source of Truth**

  * All balances derived from persisted transactions
  * No eventual consistency for core financial data

* **Backend-Centric Business Logic**

  * Frontend contains no financial rules
  * Ensures correctness and security

---

## 4. Architectural Characteristics

* **Consistency**: Strong transactional guarantees
* **Maintainability**: Clear module boundaries
* **Scalability**: Vertical scaling initially, horizontal scaling possible later
* **Security**: OAuth2/OIDC, JWT-based authorization

---

## 5. Next Steps

* C4 Level 3: Component diagrams for Account, Transaction, and Loan modules
* Sequence diagrams for critical use cases (Expense, Loan Repayment)
* Architecture Decision Records (ADR)

---

**End of Document**
