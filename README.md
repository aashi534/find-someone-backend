# Find Someone Backend

A Spring Boot REST API for trusted social and professional connections and local meetups.

## Current capabilities

- LinkedIn OpenID Connect sign-in, followed by a short-lived application JWT.
- Profiles with multiple connection purposes and an opt-in discoverability switch.
- Purpose-aware connection requests, accept/decline/cancel, and accepted connections.
- One-to-one message history over REST. Clients can poll for new messages; realtime delivery is not implemented yet.
- Meetups tagged by category and style (`INFORMAL` or `FORMAL`), alongside the existing nearby search and RSVP request flow.
- Identity-verification status is represented in profiles, but no identity-verification provider is connected. This app does not collect Aadhaar or other identity-document images.

## Stack

- Java 21, Spring Boot, Spring MVC, Spring Security, Spring Data JPA
- PostgreSQL
- LinkedIn OpenID Connect and application-issued JWTs

## Local setup

1. Create a PostgreSQL database named `find_someone` (or set `DATABASE_URL`).
2. Create a LinkedIn application, enable **Sign In with LinkedIn using OpenID Connect**, and register the callback URL `http://localhost:8080/login/oauth2/code/linkedin`.
3. Set the environment variables shown in `.env.example`. Generate a unique `APP_JWT_SECRET` with at least 32 bytes. Do not commit real secrets.
4. Run the application with Java 21 and Maven: `mvn spring-boot:run`.
5. Start sign-in at `/oauth2/authorization/linkedin`. On successful sign-in, the callback returns JSON containing an application JWT. Send it on API calls as `Authorization: Bearer <accessToken>`.

The archive did not include Maven Wrapper configuration, so use an installed Maven distribution or add the standard wrapper files before relying on `./mvnw`.

## Connection API

All routes below require a valid application JWT.

| Method | Route | Purpose |
| --- | --- | --- |
| `GET` | `/users` | List discoverable profiles |
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
| `POST` | `/connections/{id}/messages` | Send a message to an accepted connection |
| `GET` | `/connections/{id}/messages?page=0&size=50` | Read message history |

Example request body:

```json
{
  "recipientId": 42,
  "purpose": "CAREER_GUIDANCE",
  "note": "I would value your perspective on backend engineering roles."
}
```

## Trust and safety status

LinkedIn sign-in authenticates the LinkedIn account; it does not verify the member's real-world identity. The identity-verification field remains `UNVERIFIED` until a trusted provider integration is added. The professional-chat AI checks, phone/email OTP, block/report workflows, moderation tooling, and identity-provider integration are not implemented in this initial foundation. Do not treat the status field as proof that a member has been verified.

Before adding Aadhaar verification, decide on a consent-based verification provider or implement UIDAI's offline verification rules with legal/privacy review. Store verification results and the minimum required metadata, not uploaded identity documents or full Aadhaar numbers.
