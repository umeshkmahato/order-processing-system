# E-commerce Order Processing System

Spring Boot backend for order creation, retrieval, status updates, cancellation rules, and scheduled pending-order processing.

## Tech Stack
- Java 17
- Spring Boot 3
- Spring Web, Spring Data JPA, Bean Validation
- H2 database
- Lombok
- JUnit 5 + Mockito + MockMvc

## Run
```bash
mvn spring-boot:run
```

## Test
```bash
mvn test
```

## Key APIs
- `POST /api/orders`
- `GET /api/orders/{id}`
- `GET /api/orders?status=PENDING`
- `PUT /api/orders/{id}/status`
- `POST /api/orders/{id}/cancel`

## OpenAPI and Swagger
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Postman Environments
- `postman/order-processing-local.postman_environment.json`
- `postman/order-processing-dev.postman_environment.json`
- `postman/order-processing-prod.postman_environment.json`

## Scheduler
- Configurable cron in `src/main/resources/application.yml`
- Default: every 5 minutes, convert `PENDING` -> `PROCESSING`


