# fraud-detection-api

Quarkus service that scores the fraud risk of a card transaction (1-5) using the Mastercard BIN Look Up sandbox and a
few simple rules.

## Endpoints

### `POST /transactions`

Requires a JWT (`Authorization: Bearer <token>`, RS256). Optional header `X-REQUESTID`, otherwise one is generated.

Request body:

```json
{
  "bin": "411111",
  "amount": 6000.00,
  "countryCode": "PL"
}
```

Response (`200`):

```json
{
  "riskScore": 4,
  "riskLevel": "HIGH",
  "assessment": "Risk level HIGH (4/5). Card issued in USA, transaction in POL; Amount 6000.00 is at least 5000",
  "triggeredRules": [
    {
      "rule": "COUNTRY_MISMATCH",
      "points": 2,
      "reason": "Card issued in USA, transaction in POL"
    },
    {
      "rule": "HIGH_AMOUNT",
      "points": 2,
      "reason": "Amount 6000.00 is at least 5000"
    }
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
    {
      "field": "bin",
      "message": "must match \"^\\d{6,8}$\""
    }
  ],
  "requestId": "0b8a3c1e-6a54-4a4b-9a58-3e0f1b1e7f10"
}
```

| Status | `error`                                                        |
|--------|----------------------------------------------------------------|
| `400`  | `VALIDATION_FAILED` (with `violations`) or `MALFORMED_REQUEST` |
| `401`  | none (empty body, missing or invalid token)                    |
| `502`  | `UPSTREAM_REJECTED` (Mastercard rejected the lookup request)   |
| `500`  | `INTERNAL_ERROR`                                               |

`violations` is `null` for everything except `VALIDATION_FAILED`.

### Health

`GET /q/health/live` and `GET /q/health/ready` (no token needed).

## Risk rules

Each rule that fires adds points. The total maps to the risk level: 0 = `VERY_LOW` (1), 1 = `LOW` (2), 2 = `MEDIUM` (3),
3-4 = `HIGH` (4), 5+ = `VERY_HIGH` (5).

| Rule               | Points | Fires when                                                                                                            |
|--------------------|--------|-----------------------------------------------------------------------------------------------------------------------|
| `COUNTRY_MISMATCH` | 2      | The country of the card issuer (from the BIN lookup) differs from `countryCode`                                       |
| `HIGH_AMOUNT`      | 1 or 2 | `amount` is at least 1000 (1 point) or at least 5000 (2 points); thresholds are configurable and assumed to be in USD |
| `PREPAID_CARD`     | 1      | The BIN lookup says the card is prepaid                                                                               |
| `BIN_NOT_VERIFIED` | 2      | The BIN is not found, or the lookup is unavailable (then the response has `degraded: true`)                           |

If the BIN lookup is unavailable, `COUNTRY_MISMATCH` and `PREPAID_CARD` are skipped, `BIN_NOT_VERIFIED` is added and
only transaction data is assessed.

## Run locally

Requires Java 21 and Mastercard sandbox credentials, passed as environment variables (or in a `.env` file in the project
root):

- `MASTERCARD_CONSUMER_KEY`
- `MASTERCARD_KEYSTORE_PATH`
- `MASTERCARD_KEYSTORE_PASSWORD`
- `MASTERCARD_KEY_ALIAS`

```powershell
.\mvnw.cmd quarkus:dev
```

The app listens on `http://localhost:8080`. To get a demo token, run `com.koperek.security.TestTokens` from the IDE (it
prints one). Tokens are verified with the public key in `src/main/resources/publicKey.pem`. The matching private key in
`src/test/resources/privateKey.pem` is a committed demo key, so on a real deployment replace the public key (
`mp.jwt.verify.publickey.location`).

Tests need no credentials: `.\mvnw.cmd test`

## Docker

Build the image (on Linux use ./mvnw instead of .\mvnw.cmd):

```powershell
.\mvnw.cmd package -DskipTests
docker build -f src/main/docker/Dockerfile.jvm -t fraud-detection-api .


```

Run it on the server. The credentials are passed at runtime and the keystore is mounted, nothing secret is baked into
the image:

```bash
docker run --rm -p 8080:8080 \
  -e MASTERCARD_CONSUMER_KEY=<consumer-key> \
  -e MASTERCARD_KEY_ALIAS=<alias> \
  -e MASTERCARD_KEYSTORE_PASSWORD=<password> \
  -e MASTERCARD_KEYSTORE_PATH=/secrets/key.p12 \
  -v /path/to/key.p12:/secrets/key.p12:ro \
  fraud-detection-api
```

## Limitations

- BINs from other networks (e.g. Visa) are not in the Mastercard sandbox, so they are scored as `BIN_NOT_VERIFIED`.
- The sandbox returns mock data and may be unstable, so results demonstrate the integration, not real risk.
- Rules and weights are illustrative. The request carries no currency: `amount` thresholds are assumed to be in USD. The
  BIN lookup returns only the card's billing currency (`billingCurrencyDefault`), which says nothing about the
  transaction currency, so it is not used. A production version would need a `currency` field in the request and a
  currency-aware service (conversion or per-currency thresholds).
- No cache: every request calls Mastercard. No retries, rate limiting or tracing.
