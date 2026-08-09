# Event Management API

REST API for event management with JWT authentication, categories and public registrations.

## Stack

- Java 21, Spring Boot 3.5.16, Maven
- Spring Security with JWT (jjwt 0.12.6)
- Spring Data JPA + Flyway Migrations
- H2 Database (file-based)
- SpringDoc OpenAPI (Swagger UI)
- Lombok

## Quick Start

```bash
./mvnw spring-boot:run
```

The application will start at `http://localhost:8080`.

## Access

| Resource | URL |
|---------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| API Docs | `http://localhost:8080/eventos-api-docs` |
| H2 Console | `http://localhost:8080/h2-console` |

## Default Admin

| Email | Password |
|-------|-------|
| `admin@email.com` | `123456` |

## Endpoints

### Authentication

| Method | Route | Description | Auth |
|--------|------|-----------|------|
| POST | `/auth/register` | Register new user | ❌ |
| POST | `/auth/login` | Login (returns JWT) | ❌ |
| POST | `/auth/logout` | Invalidate token | ✅ |

### Events

| Method | Route | Description | Auth |
|--------|------|-----------|------|
| GET | `/events` | List events (paginated + filters) | ❌ |
| GET | `/events/{id}` | Event detail | ❌ |
| POST | `/events` | Create event | ✅ USER |
| PUT | `/events/{id}` | Update event | ✅ |
| DELETE | `/events/{id}` | Delete event | ✅ |
| PATCH | `/events/{id}/status` | Change status | ✅ |

### Participants

| Method | Route | Description | Auth |
|--------|------|-----------|------|
| POST | `/events/{id}/inscrever` | Public registration | ❌ |
| GET | `/events/{id}/participantes` | List registered participants | ✅ USER |
| POST | `/events/{id}/participantes` | Register manually | ✅ ADMIN |
| DELETE | `/events/{id}/participantes/{id}` | Remove participant | ✅ ADMIN |

### Categories

| Method | Route | Description | Auth |
|--------|------|-----------|------|
| GET | `/categories` | List categories | ❌ |
| GET | `/categories/{id}` | Detail | ❌ |
| POST | `/categories` | Create | ✅ ADMIN |
| PUT | `/categories/{id}` | Update | ✅ ADMIN |
| DELETE | `/categories/{id}` | Delete | ✅ ADMIN |

## Filters (GET /events)

| Parameter | Type | Description |
|-----------|------|-----------|
| `categoriaId` | Long | Filter by category |
| `status` | EventStatus | `UPCOMING`, `ONGOING`, `FINISHED`, `CANCELLED` |
| `busca` | String | Search by title or description |
| `dataInicio` | LocalDateTime | Start date |
| `dataFim` | LocalDateTime | End date |

## Event Status

The status is calculated automatically based on `dataHora`:

- `dataHora > now` → **UPCOMING**
- `dataHora <= now < dataHora + 2h` → **ONGOING**
- `dataHora + 2h <= now` → **FINISHED**
- Manual PATCH → **CANCELLED**