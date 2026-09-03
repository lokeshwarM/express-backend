# 🎙️ EXPRESS Backend — Intelligent Peer-Listening Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.10-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP%20%2F%20SockJS-yellow.svg?style=flat)](https://spring.io/guides/gs/messaging-stomp-websocket/)
[![AI Powered](https://img.shields.io/badge/AI-Gemini%20%2F%20OpenAI-purple.svg?style=flat&logo=openai)](https://ai.google.dev/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?style=flat&logo=docker)](https://www.docker.com/)
[![Deployment](https://img.shields.io/badge/Deploy-Render%20%7C%20AWS-blueviolet.svg?style=flat)](https://render.com)

> Production-grade, secure, and scalable backend service powering **EXPRESS** — an anonymous empathetic peer-listening and mental wellness platform. Facilitates real-time audio/video sessions, smart AI-driven listener matching, real-time WebRTC signaling, Razorpay-integrated wallet billing, and automated moderation.

---

## 🌐 Live Deployments

- **Frontend Application (Vercel)**: [https://express-frontend-weld.vercel.app](https://express-frontend-weld.vercel.app)
- **Backend Service (Render / Cloud)**: `https://<your-render-backend-url>/api`
- **WebSocket Gateway**: `wss://<your-render-backend-url>/ws`

---

## 📑 Table of Contents

- [Key Capabilities & Features](#-key-capabilities--features)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Repository Structure](#-repository-structure)
- [REST API Specification](#-rest-api-specification)
- [WebSocket & WebRTC Signaling](#-websocket--webrtc-signaling)
- [Environment Variables Configuration](#-environment-variables-configuration)
- [Local Development Setup](#-local-development-setup)
- [Docker & Containerization](#-docker--containerization)
- [Deployment Guide (AWS & Render)](#-deployment-guide-aws--render)
- [Security & Moderation](#-security--moderation)
- [License & Authors](#-license--authors)

---

## 🚀 Key Capabilities & Features

1. **Authentication & Identity Security**:
   - 2-Step OTP email verification powered by Spring Mail (SMTP).
   - Google OAuth2 token credential exchange.
   - Stateless JWT authentication with BCrypt password hashing.
   - Role-Based Access Control (`USER`, `LISTENER`, `ADMIN`).
   - Anonymous public display IDs (`User#XXXX`, `Listener#XXXX`) preserving complete user privacy.

2. **Smart AI Matching Engine**:
   - Real-time affinity scoring between user emotional state/tags and listener competencies.
   - Weighted scoring algorithm: Tag Overlap (40%), Listener Rating (30%), Mood Compatibility (20%), Experience & Historical Effectiveness (10%), Red Flag Penalties (-5 pts/flag).

3. **Real-Time Communication & WebRTC Signaling**:
   - STOMP message broker with SockJS fallback (`/ws`).
   - Peer-to-peer WebRTC SDP Offer/Answer and ICE Candidate exchange.
   - Listener availability toggle and instant incoming call dispatch popup.

4. **FinTech Engine & Double-Entry Ledger**:
   - Per-minute call duration billing with fair billing start (initiated only upon verified WebRTC media handshake).
   - Platform fee commission splitting (`SESSION_DEBIT`, `LISTENER_CREDIT`, `PLATFORM_COMMISSION`).
   - Razorpay payment order generation and cryptographic HMAC-SHA256 signature verification.
   - Listener withdrawal payout requests and wallet balance accounting.

5. **AI Emotional Intelligence Layer**:
   - Pre-session mood recording and emotion categorization.
   - Real-time empathetic listener prompt suggestions during active sessions.
   - Critical distress and crisis keyword detection with immediate safety alerts.
   - Post-session sentiment analysis, satisfaction scoring, and topic extraction.
   - Longitudinal user emotional memory and trend analysis.
   - Session quality evaluation and anomaly detection.

6. **Administration & Safety Moderation**:
   - Real-time session monitoring and audit metrics (revenue, recharge volume, active sessions).
   - Listener red-flag reporting and automatic blacklisting protection.
   - Admin oversight for user memory profiles, flagged anomalies, and transaction ledger.

---

## 🏗️ System Architecture

```
                                  ┌───────────────────────────────┐
                                  │      Client Applications      │
                                  │  (Next.js 16 Web App / PWA)   │
                                  └───────────────┬───────────────┘
                                                  │
                                HTTPS / WSS       │ JWT Authorization
                                                  ▼
┌─────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                    EXPRESS BACKEND (Spring Boot)                                │
│                                                                                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌────────────────────────────────────────┐  │
│  │    Security Filter   │  │   WebSocket Broker   │  │           REST Controllers             │  │
│  │   JwtAuthFilter +    │  │  SockJS /ws Handler  │  │  Auth, Session, AI, Listener, Wallet,  │  │
│  │    CORS Config       │  │   /topic Broadcast   │  │  Payment, Flag, Admin, Dev, Health  │  │
│  └──────────┬───────────┘  └──────────┬───────────┘  └───────────────────┬────────────────────┘  │
│             │                         │                                  │                       │
│             └─────────────────────────┼──────────────────────────────────┘                       │
│                                       ▼                                                          │
│  ┌────────────────────────────────────────────────────────────────────────────────────────────┐  │
│  │                                      Domain Services                                       │  │
│  │  AuthService │ SessionService │ SmartMatchingService │ PaymentService │ WalletService      │  │
│  │  AiSuggestionService │ SessionIntelligenceService │ OpenAiService │ WithdrawalService      │  │
│  └────────────────────────────────────┬───────────────────────────────────────────────────────┘  │
│                                       ▼                                                          │
│  ┌────────────────────────────────────────────────────────────────────────────────────────────┐  │
│  │                           Spring Data JPA & Entity Repositories                            │  │
│  │  UserRepo │ ListenerRepo │ SessionRepo │ WalletRepo │ LedgerRepo │ FlagRepo │ MemoryRepo   │  │
│  └────────────────────────────────────┬───────────────────────────────────────────────────────┘  │
└───────────────────────────────────────┼─────────────────────────────────────────────────────────┘
                                        │
             ┌──────────────────────────┼──────────────────────────┐
             ▼                          ▼                          ▼
     ┌───────────────┐          ┌───────────────┐          ┌───────────────┐
     │  PostgreSQL   │          │  SMTP Server  │          │ Gemini/OpenAI │
     │  Relational DB│          │  (Gmail Mail) │          │  LLM Engine   │
     └───────────────┘          └───────────────┘          └───────────────┘
```

---

## 💻 Tech Stack

| Layer | Technology | Purpose |
| :--- | :--- | :--- |
| **Language** | Java 21 (Eclipse Temurin LTS) | Core modern Java runtime |
| **Framework** | Spring Boot 3.5.10 | Application framework & dependency injection |
| **Security** | Spring Security 6, JJWT 0.11.5 | Stateless JWT authentication, BCrypt encryption |
| **Database & ORM** | PostgreSQL 16, Spring Data JPA, Hibernate | Persistent data storage, relational ORM |
| **Real-Time** | Spring WebSocket, STOMP, SockJS | Call signaling & listener notification dispatch |
| **AI Integration** | Google Gemini 1.5 Flash / OpenAI REST API | Emotional sentiment, live prompts, user memory |
| **Payment Gateway** | Razorpay Java SDK 1.4.5 | Recharge orders, webhook verification |
| **Mailing** | Spring Boot Starter Mail (JavaMailSender) | Secure OTP transmission via SMTP |
| **Build & Tooling** | Maven 3.9+, Docker Multi-Stage | Build automation, container deployment |

---

## 📁 Repository Structure

```
express-backend/
├── .mvn/wrapper/                  # Maven wrapper binaries & configuration
├── src/
│   ├── main/
│   │   ├── java/com/express/expressbackend/
│   │   │   ├── ExpressBackendApplication.java    # Spring Boot Main Entry Point
│   │   │   ├── api/                              # REST & WebSocket Controllers
│   │   │   │   ├── AdminController.java          # Admin oversight, metrics & blacklist
│   │   │   │   ├── AiController.java             # Tags, mood, sentiments & AI suggestions
│   │   │   │   ├── AuthController.java           # Signup, OTP, Login, Google OAuth
│   │   │   │   ├── DevController.java            # Development seeding & testing utilities
│   │   │   │   ├── FlagController.java           # Listener abuse flagging endpoint
│   │   │   │   ├── GlobalExceptionHandler.java   # Centralized error handler
│   │   │   │   ├── HealthController.java         # Liveness & readiness probes
│   │   │   │   ├── ListenerController.java       # Listener availability, reviews & queue
│   │   │   │   ├── ListenerStatsController.java  # Listener earnings & profile stats
│   │   │   │   ├── PaymentController.java        # Razorpay order generation & verification
│   │   │   │   ├── SessionController.java        # Call initiation, start, connect, end
│   │   │   │   ├── SignalingController.java      # REST WebRTC signaling fallback
│   │   │   │   ├── SignalingSocketController.java# STOMP WebSocket signaling broker
│   │   │   │   └── WithdrawalController.java     # Payout withdrawal management
│   │   │   ├── config/                           # Security, CORS & WebSocket configurations
│   │   │   │   ├── SecurityConfig.java           # JWT Filter chain, CORS policies, routes
│   │   │   │   └── WebSocketConfig.java          # STOMP endpoints & broker registry
│   │   │   └── domain/                           # Business logic, entities, repositories
│   │   │       ├── ai/                           # AI mood, tags, evaluations & memories
│   │   │       ├── auth/                         # JWT service, auth filter, DTOs
│   │   │       ├── common/                       # Shared API response DTOs & AuthUtil
│   │   │       ├── exception/                    # Custom error models
│   │   │       ├── flag/                         # Listener red-flag records & service
│   │   │       ├── ledger/                       # Double-entry ledger accounting
│   │   │       ├── listener/                     # Listener entity, queue & matching
│   │   │       ├── otp/                          # OTP token lifecycle & email dispatch
│   │   │       ├── payment/                      # Razorpay order & verification service
│   │   │       ├── review/                       # Session ratings & reviews
│   │   │       ├── session/                      # Session entity, state & timeout scheduler
│   │   │       ├── signaling/                    # WebRTC signaling entities & storage
│   │   │       ├── user/                         # User accounts, roles & profiles
│   │   │       └── wallet/                       # Balance management & withdrawal service
│   │   └── resources/
│   │       └── application.yaml                  # Application configuration & env bindings
│   └── test/                                     # Spring Boot unit & integration tests
├── Dockerfile                                    # Multi-stage production container build
├── mvnw / mvnw.cmd                               # Maven wrapper scripts
├── pom.xml                                       # Project dependencies & build plugins
└── DOCUMENTATION.md                              # Deep architectural and technical guide
```

---

## 🔌 REST API Specification

All REST endpoints (except `/api/health` and `/api/auth/**`) require the HTTP header:
`Authorization: Bearer <JWT_TOKEN>`

### 1. Authentication (`/api/auth`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/send-signup-otp` | Sends 6-digit verification OTP to email | Public |
| `POST` | `/api/auth/complete-signup` | Validates OTP and registers new user/listener | Public |
| `POST` | `/api/auth/login` | Authenticates user credentials and returns JWT | Public |
| `POST` | `/api/auth/verify-email` | Verifies account OTP (legacy flow) | Public |
| `POST` | `/api/auth/resend-otp` | Resends OTP for email verification or password reset | Public |
| `POST` | `/api/auth/forgot-password` | Sends password reset OTP | Public |
| `POST` | `/api/auth/reset-password` | Resets password with verified OTP | Public |
| `POST` | `/api/auth/google` | Google OAuth2 ID Token sign-in/registration | Public |

### 2. User & Profile Management (`/api/users`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/me` | Fetches current user profile and public display ID | Authenticated |
| `POST` | `/api/users/me/change-password`| Updates user password with current password check | Authenticated |

### 3. Session & WebRTC Call Lifecycle (`/api/sessions`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/sessions/call` | Matches listener and initiates `CREATED` session | USER |
| `POST` | `/api/sessions/{id}/start` | Listener accepts incoming call | LISTENER |
| `POST` | `/api/sessions/{id}/connected` | Records exact WebRTC media connection timestamp | Authenticated |
| `POST` | `/api/sessions/{id}/end` | Ends session, computes duration, processes billing | Authenticated |
| `GET` | `/api/sessions/active` | Retrieves current active call session (if any) | Authenticated |
| `GET` | `/api/sessions/my` | Fetches caller's historical call log | USER |
| `GET` | `/api/sessions/{id}` | Fetches detailed session metadata | Authenticated |

### 4. AI & Emotional Intelligence (`/api/ai`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/ai/user/tags` | Retrieves current user interest/topic tags | Authenticated |
| `POST` | `/api/ai/user/tags` | Updates user emotional/support interest tags | Authenticated |
| `GET` | `/api/ai/listener/tags` | Retrieves listener expertise tags | Authenticated |
| `POST` | `/api/ai/listener/tags` | Updates listener expertise tags | Authenticated |
| `POST` | `/api/ai/user/mood` | Records user mood before initiating session | Authenticated |
| `GET` | `/api/ai/user/mood` | Fetches latest logged mood | Authenticated |
| `POST` | `/api/ai/session/{id}/analyze`| Triggers post-session review sentiment analysis | Authenticated |
| `POST` | `/api/ai/session/{id}/suggest`| Generates real-time listener empathetic prompts | Authenticated |
| `GET` | `/api/ai/user/memory` | Retrieves user longitudinal emotional memory | Authenticated |
| `GET` | `/api/ai/session/{id}/evaluation`| Retrieves session quality & anomaly score | Authenticated |

### 5. Listener Management & Reviews (`/api/listeners`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/listeners/me` | Checks listener online/available status | LISTENER |
| `POST` | `/api/listeners/me/availability`| Toggles online/offline status | LISTENER |
| `GET` | `/api/listeners/me/stats` | Fetches rating, completed sessions, flag count | LISTENER |
| `GET` | `/api/listeners/me/sessions` | Fetches listener's completed call history | LISTENER |
| `GET` | `/api/listeners/me/reviews` | Fetches anonymous reviews received | LISTENER |
| `GET` | `/api/listeners/me/flags` | Fetches flag incidents associated with listener | LISTENER |
| `POST` | `/api/listeners/review` | User submits post-session rating & review | USER |

### 6. Wallet, Billing & Razorpay (`/api/wallet`, `/api/payment`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/wallet/me/balance` | Fetches current wallet balance | Authenticated |
| `GET` | `/api/wallet/me/transactions` | Fetches ledger audit history | Authenticated |
| `POST` | `/api/wallet/me/withdraw` | Submits listener payout withdrawal request | LISTENER |
| `POST` | `/api/payment/create-order` | Creates Razorpay order for wallet top-up | Authenticated |
| `POST` | `/api/payment/verify` | Verifies signature & credits wallet balance | Authenticated |

### 7. Flagging & Safety (`/api/flags`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/flags` | Submits listener red-flag during or after call | Authenticated |

### 8. Admin Control Center (`/api/admin`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/admin/stats` | Platform metrics (revenue, recharges, counts) | ADMIN |
| `GET` | `/api/admin/users` | Lists all users with balance and verification | ADMIN |
| `GET` | `/api/admin/listeners` | Lists all listeners with ratings and flag counts | ADMIN |
| `POST` | `/api/admin/listeners/{id}/blacklist` | Blacklists a problematic listener | ADMIN |
| `POST` | `/api/admin/listeners/{id}/unblacklist`| Restores a blacklisted listener | ADMIN |
| `POST` | `/api/admin/listeners/{id}/reset-flags` | Clears listener flags and resets standing | ADMIN |
| `GET` | `/api/admin/flags` | Full incident log of all submitted flags | ADMIN |
| `GET` | `/api/admin/reviews` | Moderation view of all user reviews | ADMIN |
| `GET` | `/api/admin/transactions` | Filterable double-entry transaction log | ADMIN |
| `GET` | `/api/admin/sessions` | Comprehensive session history with durations | ADMIN |
| `GET` | `/api/admin/anomalies` | Evaluated sessions with high anomaly index | ADMIN |
| `GET` | `/api/admin/user-memories`| Emotional trends & recurring stress indicators | ADMIN |

---

## ⚡ WebSocket & WebRTC Signaling

- **Endpoint**: `/ws` (supports SockJS fallback)
- **Application Prefix**: `/app`
- **Broker Prefix**: `/topic`

### Real-Time Channels:
1. **Incoming Call Popup**:
   - Destination: `/topic/listener/{listenerId}`
   - Payload: `{ "sessionId": "UUID", "type": "VOICE|VIDEO", "userDisplayId": "User#1234" }`
2. **WebRTC Signaling Broker**:
   - Publish to: `/app/signal`
   - Subscribe to: `/topic/signal`
   - Payload: `{ "type": "offer|answer|candidate", "sessionId": "UUID", "sender": "UUID", "data": {...} }`

---

## ⚙️ Environment Variables Configuration

Configure the following environment variables in your local `.env` or deployment platform:

| Variable | Description | Example / Default |
| :--- | :--- | :--- |
| `DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/express_db` |
| `DB_USERNAME` | PostgreSQL database username | `postgres` |
| `DB_PASSWORD` | PostgreSQL database password | `secretpassword` |
| `MAIL_USERNAME` | Gmail address for SMTP OTP delivery | `express.app.mailer@gmail.com` |
| `MAIL_PASSWORD` | Google App Password (16 chars) | `abcd efgh ijkl mnop` |
| `RAZORPAY_KEY_ID` | Razorpay API Key ID | `rzp_live_xxxxxxxxxxxx` |
| `RAZORPAY_KEY_SECRET` | Razorpay API Key Secret | `xxxxxxxxxxxxxxxxxxxxxxxx` |
| `GOOGLE_CLIENT_ID` | Google OAuth2 Client ID for Web | `123456789-abc.apps.googleusercontent.com` |
| `OPENAI_API_KEY` | Google Gemini / OpenAI API Key | `AIzaSy...` or `sk-...` |
| `PORT` *(Optional)* | HTTP Port (Default: 8080) | `8080` |

---

## 🛠️ Local Development Setup

### Prerequisites
- **Java 21 JDK** installed
- **PostgreSQL 14+** running locally
- **Maven 3.9+** (or use included `./mvnw`)

### 1. Clone & Configure
```bash
git clone https://github.com/lokeshwarM/express-backend.git
cd express-backend
```

### 2. Set Local Environment Variables
Create a local script or configure IDE run configuration with the variables listed above.

### 3. Build & Run
```bash
# Using Maven Wrapper on Windows PowerShell
.\mvnw.cmd clean spring-boot:run

# Using Maven Wrapper on Linux/macOS
./mvnw clean spring-boot:run
```

The server starts at `http://localhost:8080/api`. Verify health:
```bash
curl http://localhost:8080/api/health
# Response: {"success":true,"data":"OK"}
```

---

## 🐳 Docker & Containerization

Build and run using the optimized multi-stage `Dockerfile`:

```bash
# Build the Docker image
docker build -t express-backend:latest .

# Run the container
docker run -d \
  -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host.docker.internal:5432/express_db" \
  -e DB_USERNAME="postgres" \
  -e DB_PASSWORD="password" \
  -e MAIL_USERNAME="your-email@gmail.com" \
  -e MAIL_PASSWORD="your-app-password" \
  -e RAZORPAY_KEY_ID="rzp_test_xxx" \
  -e RAZORPAY_KEY_SECRET="secret" \
  -e GOOGLE_CLIENT_ID="google-client-id" \
  -e OPENAI_API_KEY="gemini-api-key" \
  --name express-backend-instance \
  express-backend:latest
```

---

## ☁️ Deployment Guide (AWS & Render)

### Historical AWS Architecture
1. **Compute**: AWS EC2 instance (Ubuntu 22.04 LTS) running Spring Boot JAR via `systemd`.
2. **Database**: AWS RDS PostgreSQL 16 Free-Tier instance.
3. **Networking**: EC2 Security Group configured to accept inbound TCP on port `8080` from Vercel proxy.

### Current Render Cloud Deployment
1. Connect GitHub repository `lokeshwarM/express-backend` to **Render**.
2. Select **Web Service** with **Docker** or **Java 21** environment.
3. Attach a **Render Managed PostgreSQL Database**.
4. In Render Environment Settings, configure:
   - `DB_URL` (from Render Internal Database URL with `jdbc:postgresql://...`)
   - `DB_USERNAME`, `DB_PASSWORD`
   - `MAIL_USERNAME`, `MAIL_PASSWORD`
   - `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`
   - `GOOGLE_CLIENT_ID`
   - `OPENAI_API_KEY`
5. Set Health Check Path to `/api/health`.

---

## 🛡️ Security & Moderation

- **Stateless Authentication**: All user actions are verified using short-lived JWT tokens signed with SHA-256.
- **Privacy Preservation**: Users and listeners only see randomly assigned pseudonyms (`User#xxxx`, `Listener#xxxx`), safeguarding personal information.
- **Fair Billing Clock**: Billing strictly starts after WebRTC media handshake confirmation (`/api/sessions/{id}/connected`), protecting users from connection drops or unresponsive listeners.
- **Automated Sanctions**: Listeners accumulating red flags are immediately deprioritized by `SmartMatchingService` and automatically blacklisted upon reaching threshold.

---

## 👥 Authors & Contributors

- **Pranavi & Lokeshwar M** — Project Architecture, Full Stack Engineering & Deployment
- GitHub: [@lokeshwarM](https://github.com/lokeshwarM) & [@Pranavi66421](https://github.com/Pranavi66421)

---

## 📜 License

This project is proprietary and confidential. Developed for the EXPRESS Mental Wellness Platform.
