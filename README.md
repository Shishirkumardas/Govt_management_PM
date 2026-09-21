# GovOS Bangladesh — AI-Powered Government Operating System

Centralized platform **proposed** for the **Prime Minister’s Office** and the Government of Bangladesh to monitor national operations, ministries, budgets, mega projects, field reporting, risks, and AI-assisted decisions.

> **Proposal status / প্রস্তাবনার অবস্থা:** This repository is a **proposal with a working demo (MVP)** — **not** a completed national production system.  
> এই রিপোজিটরি একটি **প্রস্তাবনা ও কার্যকরী ডেমো** — সম্পূর্ণ জাতীয় প্রোডাকশন সিস্টেম নয়।  
> If the **Government of Bangladesh** wants, we can **fully complete the project in 6 months** (build, secure, train, pilot, handover).  
> **বাংলাদেশ সরকার** চাইলে **৬ মাসে পূর্ণ প্রকল্প সম্পন্ন** করা যাবে (নির্মাণ, নিরাপত্তা, প্রশিক্ষণ, পাইলট, হস্তান্তর)।

> **Design principle:** Prefer automatic data feeds from existing systems + structured workflows for what cannot be automated. AI **summarizes, detects anomalies, and recommends** — it does not make final governmental decisions.

## Documentation / ডকুমেন্টেশন (Bangla + English)

**Start here for the graphic, easy-to-understand bilingual proposal:**

| File | Description |
|------|-------------|
| [**docs/GovOS_Bangladesh_Proposal.html**](docs/GovOS_Bangladesh_Proposal.html) | Full visual proposal (open in browser) |
| [**docs/GovOS_Bangladesh_Proposal.pdf**](docs/GovOS_Bangladesh_Proposal.pdf) | Printable PDF of the same document |
| [docs/SYSTEM_OVERVIEW_BN_EN.md](docs/SYSTEM_OVERVIEW_BN_EN.md) | Short bilingual system overview |
| [docs/README.md](docs/README.md) | Documentation index |

---

## Vision

Instead of collecting hundreds of manual reports, authorized leaders open a **live national dashboard**:

- Economy, budget, revenue, mega projects, emergencies  
- Agriculture → health → education → transport → power → foreign affairs  
- Daily field reports roll up into monthly/yearly statistics  
- Conversational AI answers: *“Which projects are delayed over 6 months?”*

---

## Architecture (implemented MVP)

```
Prime Minister Dashboard
        │
Cabinet / Ministry Portals
        │
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
AI Decision Engine (SpaceXAI / Grok + rule engine)
National data (JPA / H2 or PostgreSQL)
Risk detection · Notifications · KPIs
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        │
Ministries · Districts · Field officers · Citizen portal
```

| Layer | Technology |
|--------|------------|
| Backend | Java 17, Spring Boot 4, Spring Security (JWT), Spring Data JPA |
| Frontend | Next.js 15, TypeScript, Tailwind CSS |
| AI | SpaceXAI (xAI / Grok) via `XAI_API_KEY` + data-grounded fallback |
| DB (dev) | H2 in-memory |
| DB (prod-ready) | PostgreSQL driver included; Docker Compose provided |
| Future | Redis, Kafka, Elasticsearch, K8s, GraphQL, data warehouse |

---

## User levels (RBAC)

| Level | Role | Access |
|------:|------|--------|
| 1 | Prime Minister | Full national dashboard, AI, all ministries |
| 2 | Cabinet Minister | Own ministry |
| 3 | Secretary | Ministry operations |
| 4 | Department officer | Daily operations |
| 5 | District admin | District reporting |
| 6 | Field officer | Submit daily reports |

---

## Modules in this codebase

### Phase 1 (core — working)
- Identity & JWT login  
- Hierarchical roles  
- Daily reporting (work, visits, expenses, problems, GPS fields)  
- National PM dashboard (indicators, projects, alerts, FDI)  
- Ministry dashboards  
- Document registry + search  
- Notifications  

### Phase 2 (finance & projects — working demo)
- Project management (budget, spent, remaining, delay, contractor, AI risk)  
- Budget monitoring per ministry + unusual spend flags  
- Procurement with AI risk flags (price inflation, fake vendor, duplicate invoice, conflict)  

### Phase 3 (ministry integration — scaffolded)
- KPIs for Education, Agriculture, Health  
- District GIS profiles  
- Seed data for major ministries  

### Phase 4 (AI — working demo)
- National briefing  
- **Conversational assistant** (`POST /api/ai/ask`)  
- Risk engine flags  
- Predictive outlook (indicator-based)  
- Optional live Grok when `XAI_API_KEY` is set  

### Phase 5 (citizen — partial)
- Complaint submit + tracking (public APIs)  
- Placeholders for licenses, tax, NID, land  

### HR & performance
- Employee profiles (attendance, salary, performance score)  
- Office AI scores (efficiency, transparency, budget, citizen satisfaction)  

---

## Quick start

### Prerequisites
- Java 17+  
- Node.js 20+  
- Optional: Docker (PostgreSQL/Redis), `XAI_API_KEY` for live AI  

### 1. Backend (H2 demo — default)

```bash
cd backend
./gradlew.bat bootRun          # Windows
# ./gradlew bootRun            # macOS/Linux
```

API: `http://localhost:8080`

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

UI: `http://localhost:3000`

### Demo logins (password: `password123`)

| Username | Role | Notes |
|----------|------|--------|
| `pm` | Prime Minister | **MFA required** — login shows demo TOTP code |
| `agri.minister` | Agriculture Minister | |
| `finance.sec` | Finance Secretary | |
| `field.dhaka` | Field Officer | Opens **mobile field app** at `/field` |

### PostgreSQL production

```bash
# Start Postgres + optional backend container
docker compose up -d postgres redis

# Run API against Postgres
cd backend
# Windows PowerShell:
$env:SPRING_PROFILES_ACTIVE="prod"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/govos"
$env:SPRING_DATASOURCE_USERNAME="govos"
$env:SPRING_DATASOURCE_PASSWORD="govos_secret"
$env:JPA_DDL_AUTO="update"
$env:GOVOS_JWT_SECRET="Change-This-To-A-Long-Random-Secret-Key-32chars+"
.\gradlew.bat bootRun
```

Or full stack: `docker compose up --build`

### Live AI (optional)

```bash
# Windows PowerShell
$env:XAI_API_KEY="your-key"
cd backend; .\gradlew.bat bootRun
```

Without a key, AI still answers from **live warehouse data** (rule / retrieval engine).

### New capabilities (this iteration)

| Feature | Where |
|---------|--------|
| Interactive Leaflet GIS | `/gis` |
| PDF / PowerPoint national reports | `/exports` · `GET /api/exports/national.pdf` · `.pptx` |
| MFA (TOTP) for PM | Login flow · `POST /api/auth/mfa/verify` |
| Append-only audit logs | `/exports` audit table · `GET /api/exports/audit-logs` |
| Mobile field officer app | `/field` (GPS + large touch UI) |
| PostgreSQL prod profile | `application-prod.properties` + Docker Compose |

---

## Key API routes

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/login` | JWT login |
| GET | `/api/dashboard/national` | PM national dashboard |
| GET | `/api/dashboard/ministry/{id}` | Ministry dashboard |
| POST | `/api/reports/daily` | Submit daily report |
| GET | `/api/budget/overview` | Ministry budgets |
| GET | `/api/procurement` | Procurement + flags |
| GET | `/api/kpis` | Ministry KPIs |
| GET | `/api/risks` | AI risk engine |
| GET | `/api/gis/districts` | District GIS data |
| GET | `/api/documents?q=` | Document search |
| GET | `/api/hr/employees` | HR |
| GET | `/api/hr/office-scores` | Office scores |
| POST | `/api/ai/ask` | Conversational AI |
| GET | `/api/ai/briefing/national` | National AI briefing |
| POST | `/api/public/complaints` | Citizen complaint |
| GET | `/api/public/complaints/{id}` | Track complaint |

---

## Security roadmap (production)

This MVP uses JWT + role checks for demonstration. A national system must add:

- Zero Trust architecture  
- MFA + hardware-backed identity for privileged users  
- Encryption in transit (TLS) and at rest  
- RBAC + ABAC, data classification (public → secret)  
- Immutable audit logs  
- SOC integration, continuous vuln management  
- Geo-redundant disaster recovery  
- Never hard-code secrets; rotate keys; network isolation  

---

## Delivery: full project in 6 months (if sponsored)

| Months | Focus |
|--------|--------|
| 1–2 | IAM, national + ministry dashboards, daily/field reports, BN/EN polish |
| 3–4 | Budget, projects, procurement, risks, GIS, documents, PDF/PPT |
| 5–6 | AI assistant, citizen portal, training, security hardening, pilot, handover |

**Sustainable scale after go-live:** integrate finance/tax/land/health systems via feeds; staff only enter what cannot be automated; validate/approve for quality; AI assists humans.

**Proposal docs with real UI screenshots:** [docs/GovOS_Bangladesh_Proposal.html](docs/GovOS_Bangladesh_Proposal.html) · [docs/GovOS_Bangladesh_Proposal.pdf](docs/GovOS_Bangladesh_Proposal.pdf)

---

## Repository layout

```
Govt_management_PM/
├── backend/          # Spring Boot API
├── frontend/         # Next.js console
├── docs/             # Bilingual proposal documentation (HTML + PDF + MD)
├── docker-compose.yml
├── sample-briefing.pptx
├── sample-report.pdf
└── README.md
```

---

## License / note

**Proposal + demo** for architecture and product design of a national government OS.  
This is **not** a finished nationwide deployment. With Government of Bangladesh sponsorship, security accreditation, and change control, the **full project can be completed in 6 months**.

**সরকার চাইলে:** এই প্রস্তাবনার ভিত্তিতে **৬ মাসে** পূর্ণ প্রকল্প নির্মাণ ও সম্পন্ন করতে প্রস্তুত।
