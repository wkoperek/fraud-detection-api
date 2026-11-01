# fraud-detection-api

Quarkus service that scores the fraud risk of a card transaction (1-5) using the Mastercard BIN Look Up sandbox and a few simple rules.

## Endpoints

### `POST /transactions`

Requires a JWT (`Authorization: Bearer <token>`, RS256). Optional header `X-REQUESTID`, otherwise one is generated.

Request body:

```json
{ "bin": "411111", "amount": 6000.00, "countryCode": "PL" }
```

Response (`200`):

```json
{
  "riskScore": 4,
  "riskLevel": "HIGH",
  "assessment": "Risk level HIGH (4/5). Card issued in USA, transaction in POL; Amount 6000.00 is at least 5000",
  "triggeredRules": [
    { "rule": "COUNTRY_MISMATCH", "points": 2, "reason": "Card issued in USA, transaction in POL" },
    { "rule": "HIGH_AMOUNT", "points": 2, "reason": "Amount 6000.00 is at least 5000" }
  ],
  "requestId": "0b8a3c1e-6a54-4a4b-9a58-3e0f1b1e7f10",
  "degraded": false
}
```

`degraded` is `true` when the BIN lookup was unavailable.

Errors (all except `401`, which has an empty body) share one shape. Example for `400`:

```json
{
  "error": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "violations": [
    { "field": "bin", "message": "must match \"^\\d{6,8}$\"" }
  ],
  "requestId": "0b8a3c1e-6a54-4a4b-9a58-3e0f1b1e7f10"
}
```

| Status | `error` |
|---|---|
| `400` | `VALIDATION_FAILED` (with `violations`) or `MALFORMED_REQUEST` |
| `401` | none (empty body, missing or invalid token) |
| `502` | `UPSTREAM_REJECTED` (Mastercard rejected the lookup request) |
| `500` | `INTERNAL_ERROR` |

`violations` is `null` for everything except `VALIDATION_FAILED`.

### Health

`GET /q/health/live` and `GET /q/health/ready` (no token needed).

## Run locally

Requires Java 21 and Mastercard sandbox credentials, passed as environment variables (or in a `.env` file in the project root):

- `MASTERCARD_CONSUMER_KEY`
- `MASTERCARD_KEYSTORE_PATH`
- `MASTERCARD_KEYSTORE_PASSWORD`
- `MASTERCARD_KEY_ALIAS`

```powershell
.\mvnw.cmd quarkus:dev
```

The app listens on `http://localhost:8080`. To get a demo token, run `com.koperek.security.TestTokens` from the IDE (it prints one). Tokens are verified with the public key in `src/main/resources/publicKey.pem`.

Tests need no credentials: `.\mvnw.cmd test`

## Docker

Build the image (on Linux use ./mvnw instead of .\mvnw.cmd):

```powershell
.\mvnw.cmd package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t fraud-detection-api .


```

Run it on the server. The credentials are passed at runtime and the keystore is mounted, nothing secret is baked into the image:

```bash
docker run --rm -p 8080:8080 \
  -e MASTERCARD_CONSUMER_KEY=<consumer-key> \
  -e MASTERCARD_KEY_ALIAS=<alias> \
  -e MASTERCARD_KEYSTORE_PASSWORD=<password> \
  -e MASTERCARD_KEYSTORE_PATH=/secrets/key.p12 \
  -v /path/to/key.p12:/secrets/key.p12:ro \
  fraud-detection-api
```
