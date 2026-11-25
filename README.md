# Staff Management System

**One platform. Any organisation. Simplified staff management.**

A flexible, customisable Staff Management template designed for organisations of all types.

---

## Disclaimer
> [!IMPORTANT]
> This is an independent project and is only tested on a development scale.

## Features

- **Multi-Organisation Support** – Separate database for every organisation.
- **Secure Authentication** – JWT authentication and role-based access.
- **Staff Management** – Create, view, update and manage employees.
- **Leave Management** – Submit, review, approve and reject leave requests.
- **Customisable Structure** – Adapt staff information to different organisational contexts.
- **Database Isolation** – Organisation-specific MongoDB databases.

## Built With

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-brightgreen?logo=springboot)
![TypeScript](https://img.shields.io/badge/TypeScript-blue?logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-purple?logo=vite&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green?logo=mongodb)

## Getting Started

**Prerequisites**
- Java 21
- Maven
- Node.js and npm
- MongoDB Atlas account

**1. Clone the repository**

```bash
git clone https://github.com/keira-n/staff-management-system.git
```

**2. Configure the backend**

Create a `.env` file in the backend root:

```env
APP_NAME=StaffManagement
SERVER_PORT=8080
MONGODB_URI=your_mongodb_connection_string
MASTER_DATABASE=staff_management_master
JWT_SECRET=your_secure_jwt_secret
```

**3. Start the backend**

```bash
mvn spring-boot:run
```

**4. Start the frontend**

```bash
cd frontend
npm install
npm run dev
```

Open the local URL displayed by Vite.

## Architecture

A shared application with isolated organisation databases.

```text
MongoDB Atlas
│
├── staff_management_master
│   ├── organisations
│   └── user_accounts
│
├── staff_org_<organisation-id>
│   ├── Staff
│   ├── users
│   ├── leave_requests
│   └── leave_balances
│
├── staff_org_<organisation-id>
│   ├── Staff
│   ├── users
│   ├── leave_requests
│   └── leave_balances
│
└── ...
```
### Master Database

The master database stores central organisation and account directory information.

organisations – Organisation details, database names and provisioning status.

user_accounts – Central account directory containing normalised email addresses, organisation references and tenant account references.

The central account directory enforces globally unique, case-insensitive email addresses across organisations.

### Organisation Databases

Each organisation has its own database containing its operational data, including staff records, user accounts and leave information.

Organisation data is kept separate.
> [!NOTE]
> Need improvement on security. Implement it yourself or wait for me to have free time again.

## Roadmap
Things I want to add or improve in the future.

- [x] ~~Auto-generated User Account~~
- [ ] Better Database Access Security
- [ ] Payroll Management
- [ ] Performance Tracking
- [ ] Reports and Analytics
- [ ] Document Management
- [ ] Advanced Organisation Settings

## Licence

Licensed under the **[PolyForm Noncommercial License 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0/)**.

Free for permitted non-commercial use. Commercial use requires separate permission.

---