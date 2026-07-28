# Event Management API

API REST para gerenciamento de eventos com autenticação JWT, categorias e inscrições públicas.

## Stack

- Java 21, Spring Boot 3.5.16, Maven
- Spring Security com JWT (jjwt 0.12.6)
- Spring Data JPA + Flyway Migrations
- H2 Database (file-based)
- SpringDoc OpenAPI (Swagger UI)
- Lombok

## Quick Start

```bash
./mvnw spring-boot:run
```

A aplicação iniciará em `http://localhost:8080`.

## Acessos

| Recurso | URL |
|---------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| API Docs | `http://localhost:8080/eventos-api-docs` |
| H2 Console | `http://localhost:8080/h2-console` |

## Admin Padrão

| Email | Senha |
|-------|-------|
| `admin@email.com` | `123456` |

## Endpoints

### Autenticação

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| POST | `/auth/register` | Registrar novo usuário | ❌ |
| POST | `/auth/login` | Login (retorna JWT) | ❌ |
| POST | `/auth/logout` | Invalidar token | ✅ |

### Eventos

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| GET | `/events` | Listar eventos (paginado + filtros) | ❌ |
| GET | `/events/{id}` | Detalhe do evento | ❌ |
| POST | `/events` | Criar evento | ✅ USER |
| PUT | `/events/{id}` | Atualizar evento | ✅ |
| DELETE | `/events/{id}` | Excluir evento | ✅ |
| PATCH | `/events/{id}/status` | Alterar status | ✅ |

### Participantes

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| POST | `/events/{id}/inscrever` | Inscrição pública | ❌ |
| GET | `/events/{id}/participantes` | Listar inscritos | ✅ USER |
| POST | `/events/{id}/participantes` | Cadastrar manualmente | ✅ ADMIN |
| DELETE | `/events/{id}/participantes/{id}` | Remover inscrito | ✅ ADMIN |

### Categorias

| Método | Rota | Descrição | Auth |
|--------|------|-----------|------|
| GET | `/categories` | Listar categorias | ❌ |
| GET | `/categories/{id}` | Detalhe | ❌ |
| POST | `/categories` | Criar | ✅ ADMIN |
| PUT | `/categories/{id}` | Atualizar | ✅ ADMIN |
| DELETE | `/categories/{id}` | Excluir | ✅ ADMIN |

## Filtros (GET /events)

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `categoriaId` | Long | Filtrar por categoria |
| `status` | EventStatus | `UPCOMING`, `ONGOING`, `FINISHED`, `CANCELLED` |
| `busca` | String | Busca por título ou descrição |
| `dataInicio` | LocalDateTime | Data inicial |
| `dataFim` | LocalDateTime | Data final |

## Status do Evento

O status é calculado automaticamente baseado na `dataHora`:

- `dataHora > agora` → **UPCOMING**
- `dataHora <= agora < dataHora + 2h` → **ONGOING**
- `dataHora + 2h <= agora` → **FINISHED**
- PATCH manual → **CANCELLED**
