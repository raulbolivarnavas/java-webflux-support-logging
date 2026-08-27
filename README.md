# Support Logging

[![Maven Central](https://img.shields.io/maven-central/v/io.github.raulrobinson/support-logging.svg)](https://central.sonatype.com/artifact/io.github.raulrobinson/support-logging)
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
══════════════════════════════════════════════════════════════════════════════
[SUPPORT] EXTERNAL CALL
──────────────────────────────────────────────────────────────────────────────
Operation    : retrieve-customer
Method       : GET
URL          : https://api.example.com/customers/123

──────────────────────────────────────────────────────────────────────────────
QUERY PARAMS
──────────────────────────────────────────────────────────────────────────────
-

──────────────────────────────────────────────────────────────────────────────
HEADERS
──────────────────────────────────────────────────────────────────────────────
{
  "Authorization" : "Bearer ***",
  "Accept" : "application/json"
}

──────────────────────────────────────────────────────────────────────────────
REQUEST
──────────────────────────────────────────────────────────────────────────────
-

──────────────────────────────────────────────────────────────────────────────
RESPONSE
──────────────────────────────────────────────────────────────────────────────
{
  "customerId" : "123",
  "name" : "John Doe",
  "status" : "ACTIVE"
}

──────────────────────────────────────────────────────────────────────────────
CURL
──────────────────────────────────────────────────────────────────────────────
curl --location --request GET \
'https://api.example.com/customers/123' \
--header 'Authorization: Bearer ***' \
--header 'Accept: application/json'

══════════════════════════════════════════════════════════════════════════════
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
curl --location --request GET \
'https://api.example.com/customers/123' \
--header 'Authorization: Bearer ***' \
--header 'Accept: application/json'
```

For POST operations:

```bash
curl --location --request POST \
'https://api.example.com/customers' \
--header 'Content-Type: application/json' \
--data-raw '{
  "name": "John Doe"
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
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountDetailsAdapter {

    private static final ParameterizedTypeReference<AccountResponse> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClientComponent web;
    private final SupportLogCapture supportLogCapture;

    @SupportLogging(operation = "retrieve-account-details")
    public Mono<AccountResponse> retrieve(String accountNumber) {

        String endpoint = "https://api.example.com/accounts/" + accountNumber;

        Map<String, String> headers = new LinkedHashMap<>();

        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        return supportLogCapture.request("GET", endpoint, Map.of(), headers, null)
                .then(web.externalGet(endpoint, headers, RESPONSE_TYPE));
    }
}
```

---

# Configuration

Example configuration:

```yaml
support:
  logging:
    level: INFO
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

temporarily when detailed troubleshooting is required.

Avoid leaving verbose request/response logging enabled permanently when payloads
are large or contain sensitive information.

---

# Development

Clone the repository:

```bash
git clone [https://github.com/raulrobinson/support-logging](https://github.com/raulbolivarnavas/java-webflux-support-logging).git
cd support-logging
```

Build:

```bash
./mvnw clean verify
```

On Windows:

```powershell
.\mvnw.cmd clean verify
```

Install locally:

```bash
./mvnw clean install
```

The development version can then be consumed from the local Maven repository:

```text
~/.m2/repository
```

---

# Testing

Run the tests with:

```bash
./mvnw test
```

Run verification:

```bash
./mvnw clean verify
```

Before publishing a release, it is recommended to run:

```bash
./mvnw clean verify
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
5. Run `mvn clean verify`.
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