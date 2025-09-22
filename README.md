# hw-fixer

Spring Boot 3.5 project with two endpoints that convert USD↔EUR using Fixer API.

## Endpoints

- `POST /api/convert-usd-eur` — body: `{ "amount": 100 }`
- `POST /api/convert-eur-usd` — body: `{ "amount": 100 }`

## How to run

1. Put your Fixer API key to `src/main/resources/application.properties`:

```
fixer.api.key=YOUR_KEY
```

2. Build and run:

```
mvn spring-boot:run
```

3. Test:

```
curl -X POST http://localhost:8080/api/convert-usd-eur \
     -H "Content-Type: application/json" \
     -d '{"amount": 100 }'
```
