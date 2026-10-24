# Find Someone Backend

A Spring Boot REST API for trusted social and professional connections and local meetups.

The `frontend/` directory contains the initial React web app for LinkedIn sign-in, discovering profiles, connection requests and chat, meetups, and notifications.

## Current capabilities

- LinkedIn OpenID Connect sign-in, followed by a short-lived application JWT.
- Profiles with multiple connection purposes and an opt-in discoverability switch.
- Purpose-aware connection requests, accept/decline/cancel, and accepted connections.
- One-to-one message history over REST, labelled by connection purpose. Clients can poll for new messages; realtime delivery is not implemented yet.
- In-app notifications for connection requests and meetup RSVP changes, with unread counts and read controls.
- Meetups tagged by category and style (`INFORMAL` or `FORMAL`), alongside the existing nearby search and RSVP request flow.
- Identity-verification status is represented in profiles, but no identity-verification provider is connected. This app does not collect Aadhaar or other identity-document images.

## Stack

- Java 21, Spring Boot, Spring MVC, Spring Security, Spring Data JPA
- PostgreSQL
- LinkedIn OpenID Connect and application-issued JWTs

## Local setup

### Requirements

- Java 21
- Node.js 20.19+ or 22.12+
- Docker with Compose, or a local PostgreSQL 17 database
- LinkedIn developer app credentials for end-to-end sign-in

### Start PostgreSQL

From the repository root run `docker compose up -d postgres`. This starts a local database at `localhost:5432` using the values in `.env.example`. To stop it while keeping its database, run `docker compose stop postgres`.

### Configure and run the backend

1. Copy `.env.example` to `.env` and set `LINKEDIN_CLIENT_ID`, `LINKEDIN_CLIENT_SECRET`, and a unique `APP_JWT_SECRET` with at least 32 bytes. Keep `.env` private; it is ignored by Git.
2. In your LinkedIn app, enable **Sign In with LinkedIn using OpenID Connect** and register `http://localhost:8080/login/oauth2/code/linkedin` as the redirect URL.
3. In a terminal at the repository root, load the local environment and start Spring Boot:

   ```bash
   set -a
   source .env
   set +a
   ./mvnw spring-boot:run
   ```

   The API runs at `http://localhost:8080`. The first successful LinkedIn sign-in creates a profile. The backend returns the short-lived JWT to the frontend callback at `http://localhost:5173/auth/callback`.

### Run the frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. Vite proxies API calls to the backend on port 8080. The LinkedIn sign-in flow opens the backend directly, then returns to the frontend. For another backend URL, copy `frontend/.env.example` to `frontend/.env.local` and set `VITE_BACKEND_ORIGIN` and `VITE_API_BASE_URL` there.

### Try the main flows

1. Sign in with LinkedIn and complete the profile with a name, short bio, and connection purposes.
2. To test one-to-one requests, sign in as a second LinkedIn account in another browser profile, send a request from one account, and accept it from the other.
3. Open the connection and send messages. Each message is associated with a purpose accepted on that connection; the chat also has a message-report action.
4. Create a meetup from **Meetups**, then use a second account to request to join. The attendee can track or withdraw the RSVP under **My RSVPs**. The organizer can accept or decline join requests from the meetup card.
5. Use the notification bell to review connection and meetup updates. Use the member menu on a profile to block that member.

For a quick backend health check, open `http://localhost:8080/health`. The other API routes require the JWT created by LinkedIn sign-in.

## Connection API

All routes below require a valid application JWT.

| Method | Route | Purpose |
| --- | --- | --- |
| `GET` | `/users?purpose=FRIENDSHIP&query=designer` | Search discoverable profiles by purpose and name/bio text (either filter is optional) |
| `GET` | `/users/{id}` | View a discoverable profile |
| `GET` | `/users/me` | View the signed-in profile |
| `PATCH` | `/users/me` | Update name, bio, purposes, and discoverability |
| `POST` | `/connections/requests` | Send a request with recipient, purpose, and optional note |
| `GET` | `/connections/requests/incoming` | View received requests |
| `GET` | `/connections/requests/outgoing` | View sent requests |
| `PATCH` | `/connections/requests/{id}/accept` | Accept a request |
| `PATCH` | `/connections/requests/{id}/decline` | Decline a request |
| `DELETE` | `/connections/requests/{id}` | Cancel a pending request you sent |
| `GET` | `/connections` | List accepted connections |
| `POST` | `/connections/{id}/messages` | Send a message tagged with an accepted connection purpose |
| `GET` | `/connections/{id}/messages?purpose=CAREER_GUIDANCE&page=0&size=50` | Read all messages or filter history by purpose |
| `POST` | `/safety/blocks/{userId}` | Block a member and cancel pending requests between you |
| `GET` | `/safety/blocks` | List members you blocked |
| `DELETE` | `/safety/blocks/{userId}` | Unblock a member |
| `POST` | `/safety/reports` | Submit a report for moderation review |
| `POST` | `/safety/messages/{messageId}/reports` | Report a message in one of your connections |
| `GET` | `/notifications?page=0&size=20` | Read your notifications |
| `GET` | `/notifications/unread-count` | Get your unread notification count |
| `PATCH` | `/notifications/{id}/read` | Mark one of your notifications as read |
| `PATCH` | `/notifications/read-all` | Mark all your notifications as read |

## Meetup API

Meetup routes also require a valid application JWT. A member can see only their own outgoing join requests. A meetup creator can view requests for their own meetup; these endpoints return a small response DTO rather than exposing JPA entities. Accept and reject actions use the authenticated account as the actor.

| Method | Route | Purpose |
| --- | --- | --- |
| `POST` | `/meetups` | Create a formal or informal meetup |
| `GET` | `/meetups/nearby?latitude=...&longitude=...&radiusKm=5` | Find open nearby meetups |
| `POST` | `/meetups/{id}/requests` | Request to join a meetup |
| `GET` | `/meetup-requests` | View your outgoing join requests |
| `GET` | `/meetups/{id}/requests` | Organizer-only incoming request list |
| `PATCH` | `/meetup-requests/{id}/accept` | Accept a pending request as organizer |
| `PATCH` | `/meetup-requests/{id}/reject` | Reject a pending request as organizer |
| `DELETE` | `/meetup-requests/{id}` | Withdraw your pending RSVP or leave after acceptance |

When an accepted attendee leaves a full meetup, the RSVP is cancelled and the available slot is reopened. Only the requester can withdraw or leave; organizers reject pending requests through the dedicated reject action.

Example request body:

```json
{
  "recipientId": 42,
  "purpose": "CAREER_GUIDANCE",
  "note": "I would value your perspective on backend engineering roles."
}
```

Every new chat message must include the purpose it belongs to:

```json
{
  "body": "Would you be open to a short conversation about backend interviews?",
  "purpose": "CAREER_GUIDANCE"
}
```

The purpose must already be accepted on that connection. Messages created before purpose labelling was added remain available with a null purpose.

Notifications are stored in-app and can be fetched by polling the authenticated API. Email, SMS, and push delivery are not connected.

To report a member, send `reportedUserId`, a `reason` (`HARASSMENT`, `FAKE_IDENTITY`, `SCAM_OR_SPAM`, `INAPPROPRIATE_CONTENT`, or `OTHER`), and optional `details` (up to 2,000 characters). To report a specific chat message, send the same `reason` and optional `details` to `/safety/messages/{messageId}/reports`; only a member of that message's connection can report it, and the message sender is identified as the reported user. The report stores a reference to the message rather than copying its text. A block hides both profiles from discovery and connections, prevents new connection requests and messages, and can be reversed by unblocking. Reports are stored as `OPEN`; an admin review workflow is not included yet.

## Trust and safety status

LinkedIn sign-in authenticates the LinkedIn account; it does not verify the member's real-world identity. The identity-verification field remains `UNVERIFIED` until a trusted provider integration is added. This release has block and report submission workflows, but no admin review tooling, phone/email OTP, or professional-chat AI checks. Do not treat the status field as proof that a member has been verified.

Before adding Aadhaar verification, decide on a consent-based verification provider or implement UIDAI's offline verification rules with legal/privacy review. Store verification results and the minimum required metadata, not uploaded identity documents or full Aadhaar numbers.
