# Personal Finance & Loan Management System

A modular, ledger-first personal finance system focused on **financial correctness**, **clean architecture**, and **evolutionary design**.

---

## 📌 Project Goals

* Manage personal accounts and transactions
* Track expenses and income
* Manage bank loans with decreasing balance interest
* Guarantee strong financial consistency
* Avoid premature microservices complexity

---

## 🧠 Architecture Philosophy

* Modular Monolith first
* Strong consistency for money
* Ledger-style transactions
* Externalized authentication

---

## 📐 Architecture Documentation

### System & Architecture

* [System Architecture Overview (C4 Level 1 & 2)](Architecture%20–%20System%20Diagram%20%28C4%20Level%201%20%26%202%29.md)
* [C4 Level 3 – Transaction & Loan Components](C4%20Level%203%20–%20Transaction%20%26%20Loan%20Components.md)

### Domain Flows

* [Sequence Diagram – Loan Repayment](Sequence%20Diagram%20–%20Loan%20Repayment.md)

---

## 📄 Requirements

* [Software Requirements Specification (SRS)](SRS.md)

---

## 🧩 Architecture Decision Records (ADR)

* ADR-001: Modular Monolith Architecture
* ADR-002: Transaction as Single Source of Truth
* ADR-003: Google OIDC over Custom Identity
* ADR-004: Internal JWT vs Session-based Auth
* ADR-005: Transaction–Loan Consistency Strategy
* ADR-006: Shared Database vs Database per Domain
* ADR-007: Ledger-style Transaction Model

---

## 🚀 Status

This repository currently focuses on **architecture and design**.
Implementation will follow once the architecture is stabilized.

---

## 📎 Notes

This project intentionally prioritizes **correctness and clarity over speed**.

In financial systems, bugs cost trust.
