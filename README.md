# Support Logging

[![Maven Central](https://img.shields.io/maven-central/v/io.github.raulbolivarnavas/support-logging.svg)](https://central.sonatype.com/artifact/io.github.raulbolivarnavas/support-logging)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3%20%7C%204-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A lightweight logging library for Java and Spring applications designed to simplify
support, troubleshooting, and observability of external service calls.

`support-logging` provides structured request/response logging, human-readable debug
output, cURL generation, sensitive-data masking, and annotation-based logging with
minimal impact on application code.

---

## Features

- Structured logging for external HTTP calls
- Request and response logging
- Query parameter logging
- HTTP header logging
- Automatic cURL generation
- Pretty-printed JSON in debug mode
- Compact logs in info mode
- Sensitive header masking
- Annotation-based logging
- Spring dependency injection support
- WebFlux / Reactor friendly
- Configurable logging level
- Reusable across microservices

---

## Requirements

- Java 21+
- Spring Framework / Spring Boot
- Jackson Databind

The library is especially useful for Spring Boot and reactive WebFlux applications,
but its core logging components can also be reused in other Java applications.

---

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.raulbolivarnavas</groupId>
    <artifactId>support-logging</artifactId>
    <version>1.0.0</version>
</dependency>
```

Maven Central is configured by default, so no additional repository configuration
is required.

### Gradle

```gradle
implementation 'io.github.raulbolivarnavas:support-logging:1.0.0'
```

For Gradle Kotlin DSL:

```kotlin
implementation("io.github.raulbolivarnavas:support-logging:1.0.0")
```

The released artifact is available directly from Maven Central:

```gradle
repositories {
    mavenCentral()
}
```

No `mavenLocal()` repository is required for version `1.0.0`.

---

# Quick Start

## 1. Configure the library

Add the logging configuration to your `application.yml`:

```yaml
support:
  logging:
    level: INFO
```

For more detailed troubleshooting:

```yaml
support:
  logging:
    level: DEBUG
```

The configuration can also be provided using environment variables:

```bash
SUPPORT_LOGGING_LEVEL=DEBUG
```

---

## 2. Log an external operation

Inject `SupportLogCapture` into the component responsible for the external call:

```java
@Component
@RequiredArgsConstructor
public class CustomerAdapter {

    private final WebClient webClient;
    private final SupportLogCapture supportLogCapture;

    public Mono<CustomerResponse> retrieveCustomer(String customerId) {

        String endpoint =
                "https://api.example.com/customers/" + customerId;

        Map<String, String> headers = Map.of(
                "Authorization", "Bearer token",
                "Accept", "application/json"
        );

        return supportLogCapture
                .request("GET", endpoint, Map.of(), headers, null)
                .then(webClient.get()
                        .uri(endpoint)
                        .headers(httpHeaders -> headers.forEach(httpHeaders::add))
                        .retrieve()
                        .bodyToMono(CustomerResponse.class));
    }
}
```

---

# Annotation-Based Logging

The library can also identify operations using the `@SupportLogging` annotation.

```java
@SupportLogging(operation = "retrieve-customer")
public Mono<CustomerResponse> retrieveCustomer(String customerId) {
    return customerGateway.retrieve(customerId);
}
```

This keeps support-related metadata separated from business logic.

A typical operation can therefore be identified by a meaningful name:

```java
@SupportLogging(operation = "retrieve-account-details")
```

---

# Logging Modes

## INFO

`INFO` mode is optimized for production environments.

It produces compact logs containing the most important information about the
external interaction.

Example:

```text
[SUPPORT] operation=retrieve-customer |
method=GET |
url=https://api.example.com/customers/123 |
queryParams=- |
request=- |
response={"customerId":"123","status":"ACTIVE"}
```

This mode is intended to provide useful operational information without producing
large multiline logs.

---

## DEBUG

`DEBUG` mode is intended for development and troubleshooting.

Example:

```text
──────────────────────────────────────────────────────────────────────────────────
 ## [SUPPORT HTTP CALL] ##
──────────────────────────────────────────────────────────────────────────────────
- [OPERATION] : update-customer
- [METHOD]    : PUT
- [URL]       : https://jsonplaceholder.typicode.com/users/1
────────────────────── REQUEST ───────────────────────────────────────────────────
- [QUERY-PARAMS]
{
  "id" : "1",
  "cardNumber" : "******6795"
}

- [HEADERS]
{
  "Accept" : "application/json",
  "Authorization" : "************"
}

- [BODY]
{
  "customer" : {
    "name" : "Raul"
  }
}
────────────────────── RESPONSE ─────────────────────────────────────────────────
{
  "customer" : {
    "name" : "Raul"
  },
  "id" : 1
}
────────────────────── CURL ─────────────────────────────────────────────────────
curl --location --request PUT 'https://jsonplaceholder.typicode.com/users/1?id=1&cardNumber=******6795' \
--header 'Accept: application/json' \
--header 'Authorization: ************' \
--header 'Content-Type: application/json' \
--data-raw '{
  "customer" : {
    "name" : "Raul"
  }
}'
─────────────────────────────────────────────────────────────────────────────────
```

This format makes it easier to copy requests, inspect payloads and troubleshoot
external integrations.

---

# Sensitive Data

Logging credentials is dangerous, especially when logs are sent to centralized
platforms such as:

- CloudWatch
- Elasticsearch
- Splunk
- Datadog
- Grafana Loki

The library masks known sensitive headers before writing them to the log.

For example:

```text
Authorization: Bearer ***
```

instead of:

```text
Authorization: Bearer eyJhbGciOi...
```

Typical sensitive headers include:

```text
Authorization
Proxy-Authorization
X-API-Key
API-Key
```

Sensitive values should never be intentionally logged by applications consuming
this library.

---

# cURL Generation

One of the main troubleshooting features is automatic cURL generation.

Given:

```java
Map<String, String> headers = Map.of(
        "Authorization", "Bearer token",
        "Accept", "application/json"
);
```

the logger can generate:

```bash
curl --location 'http://localhost:8080/demo/customers/1' \
--header 'Authorization: Bearer a.b.c'
```

For POST operations:

```bash
curl --location 'http://localhost:8080/demo/customers' \
--header 'Authorization: Bearer a.b.c' \
--header 'Content-Type: application/json' \
--data '{
    "customer": {
        "name": "Raul",
        "customerId": "256895515411",
        "accountNumber": "45032015223158411",
        "password": "EsteEsElPassword"
    }
}'
```

For PUT operations:

```bash
curl --location --request PUT 'http://localhost:8080/demo/customers?id=1&cardNumber=1526346795' \
--header 'Authorization: Bearer a.b.c' \
--header 'Content-Type: application/json' \
--data '{
    "customer": {
        "name": "Raul"
    }
}'
```

This is particularly useful when reproducing external-service calls from tools
such as Postman, Insomnia, terminals or Kubernetes debug pods.

---

# WebFlux Usage

`support-logging` is designed to work naturally with Reactor pipelines.

Example:

```java
return supportLogCapture.request("POST", endpoint, queryParams, headers, command)
        .then(webClient.post()
        .uri(endpoint)
        .headers(httpHeaders -> headers.forEach(httpHeaders::add))
        .bodyValue(command).retrieve()
        .bodyToMono(ResponseDto.class))
        .flatMap(this::processResponse)
        .doOnError(error -> log.error("External service error: {}", error.getMessage()));
```

No blocking operation such as:

```java
.block()
```

is required.

---

# Recommended Architecture

The library works particularly well in Hexagonal / Clean Architecture projects.

A typical structure is:

```text
application
│
├── usecase
│
└── ports
     │
     ▼
infrastructure
│
├── adapters
│   ├── customer
│   ├── account
│   └── payment
│
└── support-logging
```

The adapter remains responsible for the external interaction while
`support-logging` handles the cross-cutting logging concern.

Example:

```text
Use Case
   │
   ▼
Gateway
   │
   ▼
External Adapter
   │
   ├── SupportLogCapture
   │
   └── WebClient
          │
          ▼
     External API
```

---

# Example Adapter

A complete WebFlux example:

```java
@Component
@RequiredArgsConstructor
public class CustomerClient {

    public static final String API_BASE = "https://jsonplaceholder.typicode.com/users/";

    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient.Builder webClientBuilder;
    private final SupportLogCapture supportLogCapture;

    @SupportLogging(operation = "retrieve-customer")
    public Mono<Map<String, Object>> retrieveCustomer(String customerId,
                                                      String authorizationHeader) {
        String endpoint = API_BASE + customerId;

        Map<String, String> headers = new LinkedHashMap<>();

        headers.put("Accept", "application/json");
        headers.put("Authorization", authorizationHeader);

        Map<String, String> queryParams = Map.of();

        return supportLogCapture.request("GET", endpoint, queryParams, headers,null)
                .then(webClientBuilder
                        .build()
                        .get()
                        .uri(endpoint)
                        .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                        .retrieve()
                        .bodyToMono(RESPONSE_TYPE)
                );
    }

    @SupportLogging(operation = "create-customer")
    public Mono<Map<String, Object>> createCustomer(Map<String, Object> customer,
                                                    String authorizationHeader) {
        String endpoint = API_BASE;

        Map<String, String> headers = new LinkedHashMap<>();

        headers.put("Accept", "application/json");
        headers.put("Authorization", authorizationHeader);

        Map<String, String> queryParams = Map.of();

        return supportLogCapture.request("POST", endpoint, queryParams, headers, customer)
                .then(webClientBuilder
                        .build()
                        .post()
                        .uri(endpoint)
                        .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                        .bodyValue(customer)
                        .retrieve()
                        .bodyToMono(RESPONSE_TYPE)
                );
    }

    @SupportLogging(operation = "update-customer")
    public Mono<Map<String, Object>> updateCustomer(String id,
                                                    String cardNumber,
                                                    Map<String, Object> customer,
                                                    String authorizationHeader) {
        String endpoint = API_BASE + id;

        Map<String, String> headers = new LinkedHashMap<>();

        headers.put("Accept", "application/json");
        headers.put("Authorization", authorizationHeader);

        Map<String, String> queryParams = Map.of(
                "id", id,
                "cardNumber", cardNumber
        );

        return supportLogCapture.request("PUT", endpoint, queryParams, headers, customer)
                .then(webClientBuilder
                        .build()
                        .put()
                        .uri(endpoint)
                        .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                        .bodyValue(customer)
                        .retrieve()
                        .bodyToMono(RESPONSE_TYPE)
                );
    }
}
```

---


# Practical Examples

## GET — path variable

```java
@SupportLogging(operation = "retrieve-customer")
public Mono<Map<String, Object>> retrieveCustomer(
        String customerId,
        String authorizationHeader
) {
    String endpoint =
            "https://jsonplaceholder.typicode.com/users/" + customerId;

    Map<String, String> headers = Map.of(
            "Accept", "application/json",
            "Authorization", authorizationHeader
    );

    return supportLogCapture
            .request("GET", endpoint, Map.of(), headers, null)
            .then(webClientBuilder.build()
                    .get()
                    .uri(endpoint)
                    .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                    .retrieve()
                    .bodyToMono(RESPONSE_TYPE));
}
```

A path variable is already part of the URL, so the captured query-parameter map is empty.

## POST — request body

```java
@SupportLogging(operation = "create-customer")
public Mono<Map<String, Object>> createCustomer(
        Map<String, Object> customer,
        String authorizationHeader
) {
    String endpoint = "https://jsonplaceholder.typicode.com/users";

    Map<String, String> headers = Map.of(
            "Accept", "application/json",
            "Authorization", authorizationHeader
    );

    return supportLogCapture
            .request("POST", endpoint, Map.of(), headers, customer)
            .then(webClientBuilder.build()
                    .post()
                    .uri(endpoint)
                    .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                    .bodyValue(customer)
                    .retrieve()
                    .bodyToMono(RESPONSE_TYPE));
}
```

Example payload:

```json
{
  "customer": {
    "name": "Raul",
    "customerId": "6163774",
    "accountNumber": "45032015223158411",
    "password": "secret-value"
  }
}
```

## PUT — query parameters

The query parameters must be sent in the real `WebClient` request and supplied to
`SupportLogCapture` so the support log describes the actual outbound interaction.

```java
@SupportLogging(operation = "update-customer")
public Mono<Map<String, Object>> updateCustomer(
        String id,
        String cardNumber,
        Map<String, Object> customer,
        String authorizationHeader
) {
    String baseUrl = "https://jsonplaceholder.typicode.com";
    String path = "/users";
    String endpoint = baseUrl + path;

    Map<String, String> headers = Map.of(
            "Accept", "application/json",
            "Authorization", authorizationHeader
    );

    Map<String, String> queryParams = Map.of(
            "id", id,
            "cardNumber", cardNumber
    );

    return supportLogCapture
            .request("PUT", endpoint, queryParams, headers, customer)
            .then(webClientBuilder
                    .baseUrl(baseUrl)
                    .build()
                    .put()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("id", id)
                            .queryParam("cardNumber", cardNumber)
                            .build())
                    .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                    .bodyValue(customer)
                    .retrieve()
                    .bodyToMono(RESPONSE_TYPE));
}
```

## Controller + external client

```java
@RestController
@RequestMapping("/demo")
@RequiredArgsConstructor
public class DemoController {

    private final CustomerClient customerClient;

    @GetMapping("/customers/{id}")
    public Mono<Map<String, Object>> getCustomer(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorization
    ) {
        return customerClient.retrieveCustomer(id, authorization);
    }

    @PostMapping("/customers")
    public Mono<Map<String, Object>> createCustomer(
            @RequestBody Map<String, Object> customer,
            @RequestHeader("Authorization") String authorization
    ) {
        return customerClient.createCustomer(customer, authorization);
    }
}
```

This separation fits Hexagonal/Clean Architecture: the controller handles the inbound
contract while the driven adapter/client captures the outbound HTTP interaction.

## Masking examples

```yaml
support:
  logging:
    level: DEBUG
    masking:
      enabled: true
      fields:
        authorization:
          type: FULL
        password:
          type: FULL
        clientSecret:
          type: FULL
        cardNumber:
          type: RIGHT
          visible: 4
        accountNumber:
          type: RIGHT
          visible: 4
        customerId:
          type: LEFT
          visible: 3
        email:
          type: NONE
```

| Type | Behavior | Example |
|------|----------|---------|
| `FULL` | Masks the complete value | `secret` → `******` |
| `LEFT` | Keeps characters on the left | `6163774`, visible `3` → `616****` |
| `RIGHT` | Keeps characters on the right | `1526346795`, visible `4` → `******6795` |
| `NONE` | Leaves the value unchanged | `user@example.com` → `user@example.com` |

Field-name matching is case-insensitive and normalizes hyphens and underscores, so
`clientSecret`, `client-secret`, and `client_secret` match the same configured field.


# Configuration

Example configuration:

```yaml
support:
  logging:
    level: DEBUG
    masking:
      enabled: true
      fields:
        authorization:
          type: FULL
        password:
          type: FULL
        clientSecret:
          type: FULL
        cardNumber:
          type: RIGHT
          visible: 4
        accountNumber:
          type: RIGHT
          visible: 4
        customerId:
          type: LEFT
          visible: 3
        email:
          type: LEFT
```

Supported levels:

| Level   | Description                                    |
|---------|------------------------------------------------|
| `INFO`  | Compact production-oriented logging            |
| `DEBUG` | Detailed multiline logging and cURL generation |

For Kubernetes:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: example-service
data:
  SUPPORT_LOGGING_LEVEL: "INFO"
```

Then:

```yaml
support:
  logging:
    level: ${SUPPORT_LOGGING_LEVEL:INFO}
```

---

# Production Recommendations

For production environments, the recommended configuration is:

```yaml
support:
  logging:
    level: INFO
```

Enable:

```yaml
support:
  logging:
    level: DEBUG
```

# Masking Sensitive Data

```yaml
support:
  logging:
    level: DEBUG
    masking:
      enabled: true
      fields:
        authorization:
          type: FULL
        password:
          type: FULL
        clientSecret:
          type: FULL
        cardNumber:
          type: RIGHT
          visible: 4
        accountNumber:
          type: RIGHT
          visible: 4
        customerId:
          type: LEFT
          visible: 3
        email:
          type: LEFT
```

It's enable for both levels `INFO` and `DEBUG`, but it is recommended to enable it in production environments.

---

# Development

Clone the repository:

```bash
git clone https://github.com/raulbolivarnavas/support-logging.git
cd support-logging
```

Build:

```bash
./gradlew clean build
```

On Windows:

```powershell
.\gradlew clean build
```

Install locally:

```bash
./gradlew publishToMavenLocal
```

The development version can then be consumed from the local Maven repository:

```text
~/.m2/repository
```

---

# Testing

Run the tests with:

```bash
./gradlew test
```

Run verification:

```bash
./gradlew clean build
```

Before publishing a release, it is recommended to run:

```bash
./gradlew clean build
```

and ensure that all tests, source generation and Javadocs complete successfully.

---

# Releases

The project follows Semantic Versioning.

```text
MAJOR.MINOR.PATCH
```

Examples:

```text
1.0.0
1.1.0
1.1.1
2.0.0
```

Use:

- **PATCH** for backwards-compatible bug fixes.
- **MINOR** for backwards-compatible features.
- **MAJOR** for breaking API changes.

Once a version has been published to Maven Central, that version must not be
modified. Publish a new version instead.

---

# Contributing

Contributions are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Implement the change.
4. Add or update tests.
5. Run `./gradlew clean build`.
6. Open a Pull Request.

Example:

```bash
git checkout -b feature/improve-request-logging
```

---

# Security

Do not include credentials, access tokens, private keys, customer information or
other sensitive data in issues, pull requests, examples or test fixtures.

If you discover a security issue, please avoid publishing sensitive details in a
public GitHub issue.

---

# License

This project is licensed under the Apache License 2.0.

See the [LICENSE](LICENSE) file for details.

---

# Author

**Raul Bolivar Navas**

GitHub: `raulbolivarnavas`

---

## Why Support Logging?

Troubleshooting distributed applications often requires answering four simple
questions:

```text
What operation was executed?
        ↓
What request was sent?
        ↓
What did the external service return?
        ↓
How can I reproduce the call?
```

`support-logging` provides those answers consistently while keeping logging
concerns outside the application's business logic.

---
