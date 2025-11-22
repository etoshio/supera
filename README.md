# API de Solicitação de Acesso a Módulos

Projeto de referência para o teste técnico de Desenvolvedor Java Pleno.

## Tecnologias

- Java 21
- Spring Boot 3.3.x
- Spring Data JPA
- Spring Security + JWT
- Spring Validation
- PostgreSQL 17
- H2 (testes)
- Maven
- Docker / Docker Compose
- Nginx (load balancer)
- Lombok
- Springdoc OpenAPI (Swagger)
- JUnit 5, Mockito, Instancio, JaCoCo

## Como executar com Docker

```bash
docker-compose up --build
```

- API estará exposta em: `http://localhost`
- Swagger: `http://localhost/swagger-ui.html`

## Credenciais de teste

Todos com senha **Password123!**

- `ti.user@example.com` (Departamento TI)
- `fin.user@example.com` (Departamento FINANCEIRO)
- `rh.user@example.com` (Departamento RH)
- `op.user@example.com` (Departamento OPERACOES)

## Fluxo básico

1. Autenticar:
   - `POST http://localhost/api/auth/login`
   - Body:
   ```json
   {
     "email": "ti.user@example.com",
     "password": "Password123!"
   }
   ```

2. Usar o token `Bearer` retornado para acessar:

- `GET /api/modules`
- `POST /api/requests`
- `GET /api/requests`
- `GET /api/requests/{id}`
- `POST /api/requests/{id}/cancel`

## Testes

```bash
mvn clean test
```

Relatório JaCoCo: `target/site/jacoco/index.html`

## Observação

O projeto já contém:

- Modelagem JPA básica
- Autenticação JWT com expiração de 15 minutos
- Criação de solicitação com regras principais (departamento, limite, justificativa, incompatibilidade)
- Cancelamento com histórico
- Dockerfile multi-stage
- docker-compose com 3 instâncias da app + Postgres + Nginx
