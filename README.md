# Tokenization Service (Spring Boot)

A simple tokenization and detokenization REST service built with Java Spring Boot.

This project was developed as part of a technical coding exercise.

---

## Overview

This service converts sensitive account numbers into random tokens and allows
converting them back when required.

It uses an in-memory database (H2) and does not rely on any external systems.

---

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Data JPA
- H2 In-Memory Database
- Maven
- JUnit 5

---

## Prerequisites

Before running this project, please ensure you have:

- Java 17 or later
- Git (optional, for cloning)

Check Java version:

```bash
java -version
```

### Run the Application

```bash
./mvnw spring-boot:run
```

### Run unit tests
```bash
./mvnw test
```

### Call tokenize API
curl -X POST http://localhost:3000/tokenize \
-H "Content-Type: application/json" \
-d '["<Acct1>","<Acct2>"]'

### Call detokenize API (replace tokens)
curl -X POST http://localhost:3000/detokenize \
-H "Content-Type: application/json" \
-d '["<TOKEN1>","<TOKEN2>"]'