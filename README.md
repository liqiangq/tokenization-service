# Tokenization Service (Spring Boot)

A simple tokenization and detokenization REST service built with Java Spring Boot.

This project was developed as part of a technical coding exercise.

## Overview

This service converts sensitive account numbers into random 32-character tokens and allows converting them back when required.
It uses an in-memory H2 database and does not rely on any external systems.

Current behavior:

- Tokenization is idempotent: the same account number always returns the same token.
- Detokenization validates token format before lookup.
- Invalid requests return structured JSON error responses.
- The implementation includes tests for round-trip behavior and validation edge cases.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Data JPA
- H2 In-Memory Database
- Maven
- JUnit 5

## Prerequisites

Before running this project, please ensure you have:

- Java 17 or later
- Git (optional, for cloning)

Check Java version:

```bash
java -version
```

## Run the Application

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:3000`.

## Run Tests

```bash
./mvnw test
```

## API

### Tokenize

```bash
curl -X POST http://localhost:3000/tokenize \
-H "Content-Type: application/json" \
-d '["<Acct1>","<Acct2>"]'
```

Example response:

```json
["4sR4m6R1M4m8N3qk9Q0x2Y2nP7gH1aBc","f3D7kL9mN0pQ2rS4tU6vW8xY1zA3bC5d"]
```

### Detokenize

```bash
curl -X POST http://localhost:3000/detokenize \
-H "Content-Type: application/json" \
-d '["<TOKEN1>","<TOKEN2>"]'
```

Example error response:

```json
{
  "timestamp": "2026-04-07T00:00:00Z",
  "status": 400,
  "error": "400 BAD_REQUEST",
  "message": "Invalid token format. Expected 32 characters in [0-9A-Za-z].",
  "path": "/detokenize"
}
```
