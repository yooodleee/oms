# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run application (port 8084)
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.sparta.oms.OmsApplicationTests"

# Clean build
./gradlew clean build
```

## Environment Variables

The application requires these environment variables to start:

```
DATABASE_URL=jdbc:mysql://localhost:3306/<dbname>
DATABASE_USERNAME=<user>
DATABASE_PASSWORD=<password>
```

## Architecture

This is a Spring Boot 4 / Java 17 Order Management System with two domains under `com.sparta.oms`:

**Layered structure per domain:** `controller → service → repository → entity`, with DTOs for API contracts.

### Product domain (`com.sparta.oms.product`)
CRUD for products (name, price, stock). Stock is managed atomically via a native `@Modifying` query in `ProductRepository.decreaseStock()` to prevent race conditions during concurrent orders.

### Order domain (`com.sparta.oms.order`)
Order creation deducts stock from the product atomically. Order list endpoint supports pagination via `page`/`size` query params. `OrderRepository` uses `JOIN FETCH` to eagerly load the related `Product` and avoid N+1 queries.

### Key design notes
- Schema is auto-managed by Hibernate (`ddl-auto=update`) — no migration files.
- `Order` entity has a `@ManyToOne` FK to `Product`.
- SQL logging is enabled (`show-sql=true`).
- Server port: **8084**.
