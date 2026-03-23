# 🚀 HR SaaS Management System

A multi-tenant, enterprise-grade HR management system built using **Spring Boot**, designed to handle employee lifecycle, authentication, and leave management.

\---

# 🧠 Overview

This project is a full-stack backend system that simulates a real-world **HR SaaS (Software as a Service)** platform where multiple companies can manage their employees securely within a shared application.

\---

# 🏗️ Architecture

The application follows a clean layered architecture:

* **Controller Layer** → Handles HTTP requests
* **Service Layer** → Business logic
* **Repository Layer** → Database interaction
* **Entity Layer** → Database models
* **DTO Layer** → Data transfer between layers

\---

# ⚙️ Tech Stack

* Java 17
* Spring Boot
* Spring Security (JWT Authentication)
* Spring Data JPA
* PostgreSQL
* Maven
* Lombok

\---

# 🔐 Features Implemented

## 1\. Authentication \& Authorization

* JWT-based authentication
* Secure login \& registration
* Password encryption using BCrypt
* Role-based access control (ADMIN, EMPLOYEE)

## 2\. Multi-Tenant Architecture

* Company-based data isolation
* Each user is linked to a company
* Secure access so users can only access their company data

## 3\. Company \& User Management

* Company registration
* Admin user creation during company setup
* User entity linked with roles and company

## 4\. Employee Management Module

* Create employee
* View employees
* Update employee
* Delete employee
* One-to-one mapping between Employee and User

## 5\. Leave Management System

### Features:

* Employees can apply for leave
* Admin can approve/reject leave
* Leave status tracking (PENDING, APPROVED, REJECTED)
* Company-level isolation for leave requests

### Endpoints:

* `POST /api/leaves/apply` → Apply for leave
* `GET /api/leaves/my` → View own leaves
* `PUT /api/leaves/{id}/status` → Approve/Reject leave (Admin)

\---

# 🧩 Database Design

## Core Entities:

* **Company**
* **User**
* **Role**
* **Employee**
* **Leave**

### Relationships:

* Company → Users (One-to-Many)
* Company → Employees (One-to-Many)
* User → Employee (One-to-One)
* Employee → Leave (One-to-Many)

\---

# 🔒 Security Design

* JWT Token-based authentication
* Stateless session management
* Request filtering using JWT filter
* Endpoint-level role restrictions

\---

# 🧪 API Testing

All APIs tested using:

* Postman

\---

# 🚀 Deployment (Planned / Optional)

* Dockerized backend
* Cloud deployment (Render / AWS)

\---

# ⚡ Future Enhancements

* Leave balance management
* Email notifications
* Payroll system
* Attendance tracking
* Dashboard analytics
* Frontend UI (React / HTML + JS)

\---

# 💡 Learning Outcomes

* Built a real-world SaaS backend system
* Implemented JWT authentication from scratch
* Designed multi-tenant architecture
* Applied clean code and layered architecture principles

\---

# 🏁 Status

✅ Backend Core Completed  
🚧 Enhancements in Progress

\---

# 📌 Author

Developed as a portfolio project to demonstrate backend engineering and system design skills.

