# Running the App & Triggering the Endpoints via Swagger

This covers starting the service locally and exercising both REST endpoints
through the Swagger UI, per the assignment's submission instructions.

## Prerequisites

- JDK 17
- Maven
- Docker (for the local Postgres instance)

## 1. Start Postgres

From the `kotlin/` module:

```bash
cd kotlin
docker-compose up -d
```

This starts Postgres 16 on `localhost:54321` with database `time_deposit`,
user `time_deposit`, password `time_deposit` (matching
`src/main/resources/application.yml`). On first startup, Hibernate creates
the schema (`ddl-auto: update`) and `data.sql` seeds a few demo time
deposits/withdrawals.

## 2. Start the app

```bash
mvn spring-boot:run
```

The app listens on `http://localhost:8080`.

## 3. Open Swagger UI

Navigate to:

```
http://localhost:8080/swagger-ui.html
```

This renders the OpenAPI contract auto-generated from
`TimeDepositController`'s annotations. The raw OpenAPI JSON is available at
`http://localhost:8080/v3/api-docs`.

## 4. Trigger the endpoints

Both endpoints are listed under the `time-deposit-controller` tag.

### GET `/api/time-deposits` — retrieve all time deposits

1. Expand `GET /api/time-deposits`.
2. Click **Try it out**, then **Execute**.
3. The response body is a JSON array of time deposits (seeded from
   `data.sql`), each shaped as:
   ```json
   {
     "id": 1,
     "planType": "basic",
     "balance": 1000.0,
     "days": 45,
     "withdrawals": []
   }
   ```

### POST `/api/time-deposits/update-balances` — apply monthly interest

1. Expand `POST /api/time-deposits/update-balances`.
2. Click **Try it out**, then **Execute** (no request body needed).
3. A `200` response confirms the balances were recalculated and persisted.
4. Re-run the GET endpoint above to see the updated `balance` values.

## Stopping

```bash
# stop the app: Ctrl+C in the terminal running spring-boot:run
docker-compose down   # stop and remove the Postgres container
```
