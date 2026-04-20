# LedgerX Auth

Standalone Spring Boot service for authentication: identity and OTP, account signup, WebAuthn passkeys, bearer-token sessions, and optional OAuth2 (GitHub) client configuration.

## Project structure

Sources follow the usual Maven layout under `src/main/java/com/ledgerx/auth/`:

| Area | Purpose |
| --- | --- |
| `AuthApplication.java` | Spring Boot entrypoint and `@EnableConfigurationProperties` |
| `api/controller` | REST controllers (`AuthController`, `SignupController`, `PasskeyController`) |
| `api/dto` | Request/response payloads for HTTP APIs |
| `application/service` | Application services (identify/OTP, signup, passkey flows, sessions) |
| `config` | Spring configuration (security, CORS, WebAuthn, OpenAPI, auth properties) |
| `domain/model` | Domain types used across layers |
| `infra/persistence` | JPA entities |
| `infra/repository` | Spring Data repositories and adapters |
| `infra/otp` | OTP storage and delivery (e.g. in-memory store + log sender for development) |
| `infra/webauthn` | Challenge storage for WebAuthn ceremonies |
| `security` | `SecurityConfig`, token service, filters (bearer token, optional client basic auth), principal types |
| `tool` | Small helpers (encoding, identity parsing, token generation) |

Resources:

- `src/main/resources/application.yml` — datasource, JPA, OAuth2 client placeholders, `auth.*` and `webauthn.*` settings.
- `doc/db-init.sql` — reference SQL for database setup (when not using DDL auto).

Build:

- `build.gradle.kts` — dependencies and Java plugin; Spring Boot **3.3.x** (see `settings.gradle.kts` for plugin versions).
- `settings.gradle.kts` — plugin management and Maven Central for a standalone Gradle build.

Tests live under `src/test/java/` (for example `api.AuthApiIT` for HTTP-level checks).

## Features supported

- **Identity and OTP** — Identify a user by email (or supported identity string), send OTP, verify OTP (`/auth` … and mirrored `/api/auth` … paths).
- **Signup** — Create account via signup token flow (`create-account`).
- **WebAuthn / passkeys** — Registration (options + finish) and authentication (options + finish) using Yubico WebAuthn; configurable RP ID, origins, and challenge TTL (`webauthn.*` in `application.yml`).
- **Sessions / API access** — Bearer token filter for authenticated requests; optional HTTP Basic validation for trusted clients (`auth.basic-auth-client.*`, disabled by default).
- **OAuth2 client** — GitHub registration is declared in config for browser/OAuth flows; requires `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` when used.
- **Persistence** — MySQL via Spring Data JPA (configure `spring.datasource.*` for your environment).
- **API documentation** — SpringDoc OpenAPI with a dedicated **auth** group (paths under `/auth/**`, `/api/auth/**`, `/auth/passkey/**`, `/api/passkey/**`).

## Run the service

From this directory (with Gradle on your `PATH`, or after generating the Gradle wrapper):

```bash
gradle bootRun
```

Default HTTP port is **8080** unless you set `server.port` in configuration.

Ensure MySQL (or your configured database) is reachable and schema matches expectations; adjust `spring.datasource.*` in `application.yml` or override via environment / profile-specific YAML.

## OpenAPI / Swagger UI

Spring Security permits unauthenticated access to the docs endpoints (see `SecurityConfig`).

After the app is running:

| What | URL |
| --- | --- |
| **Swagger UI** | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| **OpenAPI JSON (default)** | [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) |
| **OpenAPI JSON (auth group)** | [http://localhost:8080/v3/api-docs/auth](http://localhost:8080/v3/api-docs/auth) |

The **auth** group is defined in `AuthOpenApiConfiguration` and scopes the documented paths to the LedgerX Auth REST APIs. In Swagger UI you can pick the **auth** group from the dropdown if multiple groups are present.

Replace `localhost` and port `8080` if you deploy elsewhere or change `server.port`.
