# JWT,OAuth2,KeyCloak Banking Backend

A secured REST API for a retail banking frontend, built with Spring Boot 4 and Spring Security 7.

[![CI](https://github.com/YOUR_GITHUB_USERNAME/oauth-banking-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_GITHUB_USERNAME/oauth-banking-backend/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-25-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

---

## What this is

A backend for a banking web client. A customer registers, logs in, and reads their
own account, transaction history, cards and loans. Anonymous visitors can read
public notices and submit a contact form.

The interesting part is not the CRUD. It is the security chain: a stateless
JWT-authenticated API with role-based authorization, CSRF protection for the
cookie-based paths, ownership checks on every record lookup, and JSON error
responses in place of the servlet container's defaults.

> **On the name.** Authentication is currently a hand-rolled JWT implementation
> on top of HTTP Basic, not an OAuth 2.0 flow. The repository keeps its original
> name; migrating to a proper OAuth 2.0 resource server is the first item on the
> roadmap. Calling this an OAuth implementation today would be inaccurate.

## Table of contents

- [Architecture](#architecture)
- [Security model](#security-model)
- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [API reference](#api-reference)
- [Trying it out](#trying-it-out)
- [Configuration](#configuration)
- [Database](#database)
- [Testing](#testing)
- [Project layout](#project-layout)
- [Profiles](#profiles)
- [Archived code](#archived-code)
- [Roadmap](#roadmap)
- [Known limitations](#known-limitations)
- [License](#license)

---

## Architecture

```
                    ┌──────────────────────────────────────────────┐
   Browser/cURL ──▶            Spring Security filter chain        │
                    │                                              │
                    │  (RequestValidationBeforeFilter)             │
                    │      rejects malformed Basic credentials     │
                    │  (JwtTokenValidatorFilter)                   │
                    │      verifies the bearer token, populates    │
                    │      the SecurityContext, answers 401 itself │
                    │  (BasicAuthenticationFilter)                 │
                    │  (CsrfCookieFilter)                          │
                    │      materialises the XSRF-TOKEN cookie      │
                    │  (AuthoritiesLoggingAfterFilter)             │
                    │  (JwtTokenGeneratorFilter)                   │
                    │      issues a token on /user                 │
                    │  (ExceptionTranslationFilter)                │
                    │      → CustomAuthenticationEntryPoint (401)  │
                    │      → CustomAccessDeniedHandler      (403)  │
                    │  (AuthorizationFilter)                       │
                    └───────────────────┬──────────────────────────┘
                                        │
                              ┌─────────▼──────────┐
                              │    Controllers     │
                              │  HTTP translation  │
                              └─────────┬──────────┘
                                        │
                              ┌─────────▼──────────┐
                              │      Services      │
                              │  business logic +  │
                              │  ownership check   │
                              └─────────┬──────────┘
                                        │
                              ┌─────────▼──────────┐
                              │ Spring Data JPA    │
                              │      MySQL 8       │
                              └────────────────────┘
```

## Security model

| Concern | How it is handled |
|---|---|
| **Authentication** | `BankUsernamePwdAuthenticationProvider` verifies the submitted password against a bcrypt hash loaded by `BankUserDetailsService`. E-mail is the login name. |
| **Password storage** | `DelegatingPasswordEncoder`. Hashes carry an algorithm prefix (`{bcrypt}…`) so the algorithm can be upgraded without invalidating existing passwords. |
| **Session state** | None. `SessionCreationPolicy.STATELESS`; every request carries its own proof. |
| **Tokens** | HS256 JWTs issued by `JwtService`, valid for 8 hours, carrying the username and granted authorities. The signing secret is resolved once at startup and the application refuses to start under `prod` while the built-in development secret is still in use. |
| **Authorization** | Role-based, declared in `SecurityConfiguration`. `hasRole("USER")` matches the authority `ROLE_USER` from the `authorities` table. |
| **Record ownership** | `CustomerAccessService` confirms that the `id` in the query string belongs to the authenticated caller before any record is read. Without this, authentication alone would let any customer read any other customer's statements by editing the URL. |
| **CSRF** | `CookieCsrfTokenRepository.withHttpOnlyFalse()` so the SPA can echo the token. Exempt on `/register`, `/apiLogin` and `/contact`, which are reached before a token exists. |
| **CORS** | Origins come from `app.cors.allowed-origins`; credentials allowed, `Authorization` exposed. |
| **Transport** | HTTPS enforced for every request under the `prod` profile. |
| **Error responses** | `CustomAuthenticationEntryPoint` and `CustomAccessDeniedHandler` return structured JSON instead of an empty body or an HTML error page. |
| **Audit** | `AuthenticationEvents` and `AuthorizationEvents` log every success, failure and denial. |

## Tech stack

| Layer | Choice                                              |
|---|-----------------------------------------------------|
| Language | Java 25                                             |
| Framework | Spring Boot 4.1.0, Spring Security 7                |
| Persistence | Spring Data JPA, Hibernate, MySQL 8                 |
| Tokens | JJWT 0.13.0                                         |
| Build | Maven                                               |
| Testing | JUnit 5, Mockito, AssertJ, Spring Security Test, H2 |
| CI | GitHub Actions/ Jenkins                             |

## Getting started

### Prerequisites

- JDK 25
- Docker (for MySQL), or a MySQL 8 server you already run

### Run it

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/OAuth2-Authorization-Server-Secure-Banking-Backend.git
cd OAuth2-Authorization-Server-Secure-Banking-Backend

# Start MySQL. The schema in docs/db is applied on first start.
docker compose up -d

# Run the application on http://localhost:8080
./mvnw spring-boot:run
```

Without Docker, create the database by hand and point the application at it:

```bash
mysql -u root -p < docs/db/schema.sql
DATABASE_PASSWORD=your-password ./mvnw spring-boot:run
```

### Run the tests

```bash
./mvnw test
```

The suite uses an in-memory H2 database. No MySQL server is needed.

## API reference

| Method | Path | Auth | Authority | Description |
|---|---|---|---|---|
| `GET` | `/notices` | none | — | Active public notices |
| `POST` | `/contact` | none | — | Submit a support enquiry |
| `POST` | `/register` | none | — | Create a customer |
| `POST` | `/apiLogin` | none | — | Exchange credentials for a JWT |
| `GET` | `/user` | Basic | authenticated | Current customer; also issues a fresh JWT |
| `GET` | `/account?id={customerId}` | JWT | `ROLE_USER` | Account details |
| `GET` | `/balance?id={customerId}` | JWT | `ROLE_USER` or `ROLE_ADMIN` | Transaction history |
| `GET` | `/cards?id={customerId}` | JWT | `ROLE_USER` | Issued cards |
| `GET` | `/loans?id={customerId}` | JWT | `ROLE_USER` | Loans held |

The `id` parameter must be the caller's own customer id. Any other value returns
`403`, regardless of whether that customer exists.

### Error format

Every security rejection returns the same shape:

```json
{
  "timestamp": "2026-08-28T10:15:30.123",
  "status": 403,
  "error": "Forbidden",
  "message": "The requested customer id is not available to this user",
  "path": "/account"
}
```

## Trying it out

```bash
# 1. Register
curl -X POST http://localhost:8080/register \
     -H 'Content-Type: application/json' \
     -d '{"name":"Alice","email":"alice@example.com",
          "mobileNumber":"9876543210","pwd":"Password@12345","role":"user"}'

# 2. Grant an authority. Registration deliberately grants nothing, so without
#    this the customer can log in but every protected endpoint returns 403.
mysql -u root -p bankingbackend \
  -e "INSERT INTO authorities (customer_id, name) VALUES (1, 'ROLE_USER');"

# 3. Log in and capture the token
TOKEN=$(curl -s -D - -o /dev/null -X POST http://localhost:8080/apiLogin \
     -H 'Content-Type: application/json' \
     -d '{"username":"alice@example.com","password":"Password@12345"}' \
     | grep -i '^authorization:' | cut -d' ' -f2 | tr -d '\r')

# 4. Read your own data
curl -H "Authorization: $TOKEN" 'http://localhost:8080/account?id=1'

# 5. Try to read someone else's — this returns 403
curl -i -H "Authorization: $TOKEN" 'http://localhost:8080/account?id=2'
```

## Configuration

Every setting has a development default except `JWT_SECRET` under `prod`.
See [`.env.example`](.env.example).

| Variable | Default                 | Notes |
|---|-------------------------|---|
| `DATABASE_HOST` | `localhost`             | |
| `DATABASE_PORT` | `3306`                  | |
| `DATABASE_NAME` | `bankingbackend`        | |
| `DATABASE_USERNAME` | `root`                  | |
| `DATABASE_PASSWORD` | `xyz`                   | Required under `prod` |
| `JWT_SECRET` | built-in dev value      | **Required under `prod`** — the application refuses to start without it. At least 32 characters. |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Comma-separated |
| `SPRING_SECURITY_LOG_LEVEL` | `DEBUG`                 | `TRACE` prints the whole filter chain per request |

Run in production mode with:

```bash
java -jar target/oauth-banking-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## Database

Schema: [`docs/db/schema.sql`](docs/db/schema.sql). Demo data:
[`docs/db/seed.sql`](docs/db/seed.sql).

`spring.jpa.hibernate.ddl-auto` is `none` — the schema is owned by the SQL file,
not generated at runtime.

**One thing to get right:** rows in `authorities.name` must carry the `ROLE_`
prefix (`ROLE_USER`, `ROLE_ADMIN`). The security rules use `hasRole(...)`, which
prepends `ROLE_` before comparing. A row containing bare `USER` authenticates the
customer and then denies them every protected endpoint, which is a confusing
failure to debug.

## Testing

```bash
./mvnw test                                        # everything
./mvnw test -Dtest='*ServiceTest'                  # unit tests only
./mvnw test -Dtest=SecurityFlowIntegrationTest     # end-to-end security
```

| Suite | What it covers |
|---|---|
| `JwtServiceTest` | Token round-trip, tampering, wrong signing key, refusal of the dev secret under `prod` |
| `BankUsernamePwdAuthenticationProviderTest` | That the password is actually verified, and that the raw credential is not retained |
| `CustomerAccessServiceTest` | The ownership guard, including that denials leak nothing about which ids exist |
| `BankUserDetailsServiceTest` | Authority mapping, blank and missing authority rows |
| `RequestValidationBeforeFilterTest` | Malformed Basic credentials answered with `400`, not `500` |
| `JwtTokenFilterTest` | Token issue and verification, and that an invalid token yields `401` from inside the chain |
| `CsrfCookieFilterTest` | Missing CSRF attribute no longer throws |
| `JsonErrorResponseWriterTest` | JSON escaping of hostile messages |
| `DataServiceOwnershipTest` | That every data service refuses another customer's id **before** touching the repository |
| `AuthenticationServiceTest`, `CustomerServiceTest` | Login token contents, password hashing on registration |
| `BankUsernamePwdProdAuthenticationProviderTest` | That the production provider agrees with the development one on the basics |
| `AccountControllerTest`, `ContactControllerTest`, `NoticeControllerTest` | Controller behaviour in isolation |
| `SecurityFlowIntegrationTest` | Login → token → protected endpoint → cross-customer denial, against H2 |

## Project layout

```
src/main/java/com/example/OAuthBankingBackendApplication/
├── configuration/   SecurityConfiguration (!prod), SecurityProdConfiguration (prod),
│                    AuthenticationConfig (profile-neutral beans)
├── constants/       ApplicationConstants
├── controller/      REST endpoints — request/response translation only
├── dto/             LoginRequestDTO, LoginResponseDTO
├── entity/          JPA entities
├── events/          Authentication and authorization audit listeners
├── filter/          The five custom servlet filters
├── repository/      Spring Data repositories
├── security/        Authentication providers, entry point, access denied handler
└── service/         Business logic (see below)
```

The layers are strict: a controller talks to a service, a service talks to a
repository, and nothing skips a step.

| Service | Kind | Responsibility |
|---|---|---|
| `AccountService` | domain | Account enquiries |
| `BalanceService` | domain | Statement enquiries |
| `CardsService` | domain | Card enquiries |
| `LoansService` | domain | Loan enquiries |
| `NoticeService` | domain | Public notices |
| `ContactService` | domain | Contact form, service request numbering |
| `CustomerService` | domain | Registration and customer lookup |
| `AuthenticationService` | domain | Credentials in, signed token out |
| `CustomerAccessService` | cross-cutting | The ownership rule, used by every domain service that takes a customer id |
| `JwtService` | cross-cutting | Issues and verifies tokens; used by the controllers and the two JWT filters |
| `BankUserDetailsService` | Spring SPI | Implements Spring Security's `UserDetailsService` |

The ownership check runs inside the domain services, not in the controllers.
That way a second controller, a scheduled job or a future GraphQL resolver
reaching for account data gets the check automatically instead of having to
remember it.

## Profiles

Two profile-scoped security configurations are kept deliberately, to make the
mechanism visible:

| | `SecurityConfiguration` | `SecurityProdConfiguration` |
|---|---|---|
| Active when | any profile except `prod` | `prod` |
| HTTPS | redirect disabled | enforced on every request |
| Everything else | identical | identical |

The same split applies to `BankUsernamePwdAuthenticationProvider` and
`BankUsernamePwdProdAuthenticationProvider`. `AuthenticationConfig` injects the
`AuthenticationProvider` interface rather than a concrete class, so exactly one
is a bean at a time and the profile switch actually takes effect.

The trade-off is real and worth knowing: two near-identical configuration classes
drift. Anything that must hold under every profile — the `PasswordEncoder` and
`AuthenticationManager` beans — belongs in the unprofiled `AuthenticationConfig`
instead. A single-class alternative is archived at the bottom of
`SecurityConfiguration`.

## Archived code

Most classes end with an `ARCHIVED` section: the earlier version of that class,
commented out, with a note on what it did and why it changed. It is reference
material for revising the concepts, and none of it compiles or runs.

Twenty-four files carry one. The ones worth re-reading first:

| File | What the archived block shows |
|---|---|
| `JwtTokenValidatorFilter` | Why throwing from a filter placed before `ExceptionTranslationFilter` produces a 500 instead of a 401 |
| `JwtTokenGeneratorFilter` | Why `getEnvironment()` in a non-bean filter never sees `application.properties` |
| `AccountController` | The insecure direct object reference, and why `authorizeHttpRequests` cannot catch it |
| `SecurityConfiguration` | In-memory and JDBC users, session control, authority- vs role-based rules |
| `SecurityProdConfiguration` | The commented-out beans that stopped the `prod` profile from starting |
| `UserController` | Three generations of the registration and login code |
| `AuthorizationEvents` | How SLF4J silently discards surplus arguments |

## Roadmap

- Replace the hand-rolled JWT filters with `spring-boot-starter-oauth2-resource-server` and a real authorization server
- Short-lived access tokens plus refresh tokens, and a revocation list
- Move money amounts from `int` to `BigDecimal` with an explicit scale
- Flyway migrations instead of a hand-applied schema file
- Bean Validation on request bodies
- OpenAPI documentation via springdoc
- Rate limiting on `/apiLogin` and account lockout after repeated failures
- Assign a default authority at registration rather than requiring a manual insert
- Collapse the profile-scoped security configuration pair once the profile mechanism no longer needs demonstrating

## Known limitations

This is a learning project, and these are deliberate gaps rather than oversights:

- **No refresh tokens.** An 8-hour access token is too long for real banking, and there is no way to revoke one before it expires.
- **No request validation.** Request bodies are trusted; `/register` will accept a malformed e-mail.
- **Registration grants no authorities.** A new customer must be granted `ROLE_USER` by hand before they can read anything.
- **Amounts are integers.** Fine for whole rupees, wrong for anything requiring exact decimal arithmetic.
- **`RequestValidationBeforeFilter` blocks any username containing `test`.** That is demo behaviour carried over from the original exercise and would block legitimate customers.
- **No rate limiting.** `/apiLogin` can be brute-forced.

## License

MIT — see [LICENSE](LICENSE).
