# Time Deposit Kata

## What this repo is

A take-home refactoring kata for XA Bank's time deposit system. A junior
developer wrote the interest-calculation domain logic but never built the API
or persistence layer. The task is to add both **without changing the
behavior or signature of the existing calculator**. Full requirements live in
`README.md` — read it before making structural decisions; this file only
summarizes what matters for day-to-day edits.

Only the `kotlin/` module is active. `java/`, `c#/`, `python/`, and
`typescript/` variants existed in earlier history but were deleted (see
commit `2039a05 Cleanup`) — do not resurrect them.

## Current state (as of this writing)

`kotlin/` is a Maven + Spring Boot project (JDK 17, Kotlin 1.7.20, Spring
Boot 3.1.5). The domain calculator is untouched; a hexagonal scaffold has
been added around it, but the business logic inside the adapters is still
minimal/skeletal — treat this as infrastructure setup, not a finished
implementation.

- `src/main/kotlin/org/ikigaidigital/TimeDeposit.kt` — the domain model
  (unchanged).
- `src/main/kotlin/org/ikigaidigital/TimeDepositCalculator.kt` — the interest
  engine, entry point `updateBalance(xs: List<TimeDeposit>)` (unchanged).
- `src/main/kotlin/org/ikigaidigital/TimeDepositApplication.kt` — Spring Boot
  entry point (`@SpringBootApplication`).
- `src/main/kotlin/org/ikigaidigital/domain/` — `Withdrawal` and
  `TimeDepositRecord` (a `TimeDeposit` + its withdrawals), kept separate from
  `TimeDeposit` so the calculator's input type stays untouched.
- `src/main/kotlin/org/ikigaidigital/application/port/input/` —
  `UpdateTimeDepositBalancesUseCase`, `GetAllTimeDepositsUseCase` (inbound
  ports, one per REST endpoint).
- `src/main/kotlin/org/ikigaidigital/application/port/output/` —
  `TimeDepositRepositoryPort` (outbound port for persistence).
- `src/main/kotlin/org/ikigaidigital/application/service/TimeDepositService.kt`
  — implements both use cases; the only place that calls
  `TimeDepositCalculator.updateBalance`.
- `src/main/kotlin/org/ikigaidigital/adapter/input/web/` —
  `TimeDepositController` (the two endpoints) and its response DTOs.
- `src/main/kotlin/org/ikigaidigital/adapter/output/persistence/` — JPA
  entities (`TimeDepositEntity`, `WithdrawalEntity`), Spring Data
  repositories, and `TimeDepositPersistenceAdapter` implementing the
  outbound port. `WithdrawalEntity.timeDeposit` is a real `@ManyToOne`
  association (not a plain `Int` column) because Hibernate requires the
  `mappedBy` side of `TimeDepositEntity.withdrawals`'s `@OneToMany` to be an
  actual association back to the owner; a `timeDepositId` computed property
  exposes the FK value for domain mapping. `TimeDepositJpaRepository`
  exposes `findAllEagerly()` (a `LEFT JOIN FETCH` query) so the GET endpoint
  loads withdrawals in one round trip instead of N+1 lazy loads.
- `src/main/resources/application.yml` — Postgres datasource config,
  `ddl-auto: update` (no Flyway/Liquibase set up — see comment in the file),
  `defer-datasource-initialization: true` (so `data.sql` runs after
  Hibernate creates the schema, not before — otherwise the seed insert
  fails with "relation does not exist"), springdoc/swagger-ui path.
- `src/main/resources/data.sql` — demo seed rows (there's no endpoint for
  creating time deposits/withdrawals, so this is the seam used to get data
  into the DB for manual/swagger testing).
- `docker-compose.yml` — local Postgres for `mvn spring-boot:run`.
- `src/test/kotlin/org/ikigaidigital/TimeDepositCalculatorTest.kt` — real
  unit tests covering every domain rule (30-day blackout, per-plan rates,
  premium's 45-day threshold, student's 366-day cutoff, HALF_UP rounding,
  independent mutation across a list).
- `src/test/kotlin/org/ikigaidigital/application/service/TimeDepositServiceTest.kt`
  — unit test for `TimeDepositService` against an in-memory fake of
  `TimeDepositRepositoryPort`.
- `src/test/kotlin/org/ikigaidigital/adapter/output/persistence/TimeDepositPersistenceAdapterTest.kt`
  — unit test for `TimeDepositPersistenceAdapter`'s entity<->domain mapping
  (`findAll`/`saveAll`), with `TimeDepositJpaRepository` mocked via Mockito.
- `src/test/kotlin/org/ikigaidigital/adapter/input/web/TimeDepositControllerTest.kt`
  — `@WebMvcTest` slice test for `TimeDepositController` (use cases mocked).
- `src/test/kotlin/org/ikigaidigital/TimeDepositApiIntegrationTest.kt` —
  `@SpringBootTest` + testcontainers Postgres, both REST endpoints exercised
  end-to-end through MockMvc against a real database; requires Docker.

Not yet done: the OpenAPI contract is whatever springdoc auto-generates (no
manual annotations/spec written yet); the README's submission section asks
for instructions on triggering the endpoints via Swagger, which don't exist
anywhere in the repo yet (no top-level run/usage README section).

## Domain rules already encoded (do not silently change these)

`TimeDepositCalculator.updateBalance` computes monthly interest per plan
type, using each deposit's `days` field as "days since opening":

- No interest at all for the first 30 days, regardless of plan.
- **basic**: 1% monthly interest once `days > 30`.
- **student**: 3% monthly interest once `days > 30`, but interest stops
  entirely once `days >= 366` (no interest after 1 year).
- **premium**: 5% monthly interest, but only once `days > 45` (not 30).
- Interest is `balance * rate / 12`, rounded to 2 decimals with
  `RoundingMode.HALF_UP`, then added to `balance` in place (mutates the list
  elements).

If you extend this (e.g. new plan types, an OO/strategy refactor), preserve
these exact thresholds and rounding behavior — the existing test fixture and
grading likely depend on numerically identical output for these three plans.

## Hard constraints (breaking these fails the assignment)

- **Do not change the `TimeDeposit` class or the `updateBalance` method
  signature** in a way that breaks existing callers — `updateBalance` must
  keep accepting `List<TimeDeposit>` and returning `Unit`/mutating in place.
  Wrap or compose around it instead of rewriting it.
- **Exactly two REST endpoints, no more:**
  1. Update balances of all time deposits (triggers the interest
     calculation and persists the result).
  2. Retrieve all time deposits, each shaped as
     `{ id, planType, balance, days, withdrawals }`.
- Do not add extra endpoints "for convenience" (health checks, per-id fetch,
  etc.) — the README explicitly caps it at two.
- Input validation / exception handling is explicitly **out of scope** —
  don't add defensive validation layers unless asked; it adds surface area
  the grader isn't expecting.
- Ambiguities should be resolved with a logical assumption noted in a code
  comment, not by asking to expand scope.

## Expected shape of the solution (per README's preferred stack)

- **Persistence**: two tables — `timeDeposits(id, planType, days, balance)`
  and `withdrawals(id, timeDepositId, amount, date)`, FK
  `withdrawals.timeDepositId -> timeDeposits.id`. Postgres, mapped via JPA
  entities under `adapter/output/persistence/`; table names are quoted
  (`` `timeDeposits` ``/`` `withdrawals` ``) in `@Table` to preserve the exact
  camelCase from the README instead of Postgres folding to lowercase.
- **Architecture**: Hexagonal (ports & adapters), already scaffolded — see
  "Current state" above for the package layout. `TimeDepositCalculator`'s
  domain logic stays isolated from web/persistence concerns; the two REST
  endpoints and the DB are adapters around it via `TimeDepositService`.
- **Framework**: Spring Boot 3.1.5 (`spring-boot-starter-web`,
  `spring-boot-starter-data-jpa`), chosen because it's the requested
  framework and is JDK 17 / Kotlin 1.7.20 compatible. `kotlin-maven-plugin`
  has the `spring`/`jpa` compiler plugins enabled (all-open/no-arg) so
  `@Component`/`@Entity` Kotlin classes work without being declared `open`.
- **API contract**: OpenAPI/Swagger-first — `springdoc-openapi-starter-webmvc-ui`
  is on the classpath (swagger-ui at `/swagger-ui.html`); no manual
  annotations/spec written yet beyond what it auto-generates from the
  controller.
- **Testing**: testcontainers (`org.testcontainers:testcontainers-postgresql`,
  `org.testcontainers:testcontainers-junit-jupiter`,
  `spring-boot-testcontainers`) for anything touching a real database, JUnit 5
  + AssertJ for unit tests, and Mockito for mocking Spring Data repository
  ports — see the test files listed under "Current state" above. `pom.xml`'s
  `dependencyManagement` imports `testcontainers-bom` *before*
  `spring-boot-dependencies` deliberately: Maven's BOM import takes the first
  declaration it sees for a given GA, so the reverse order silently pins
  testcontainers to the older version `spring-boot-dependencies` bundles,
  whose docker-java client defaults to a Docker Engine API version that
  newer Docker Desktop daemons reject with HTTP 400. The `junit-jupiter`/
  `postgresql` testcontainers modules also need the `testcontainers-`
  prefixed artifact IDs (renamed in 2.x) or they silently resolve to that
  same stale version too.
- **Commits**: atomic, one logical change per commit.
- **Code quality**: SOLID principles and clean-code practices, applied
  pragmatically — this is a small kata, not a platform; don't over-engineer
  abstractions beyond what the two endpoints and three plan types need.

## AI-assisted development

The assignment requires documenting whatever AI harness/agent workflow is
used (tools, config, which parts were AI-assisted and why). This is tracked
in `AI_ASSISTANCE.md` at the repo root.

**Every agent that makes changes in this repository must append an entry
to `AI_ASSISTANCE.md`** (template included in that file) before finishing
its work, describing the task, what was changed, the AI's contribution,
and why AI was used. Treat a session that leaves this log unupdated as
incomplete work — same rule as keeping this file current, below.

## Keep this file current

**Any agent that changes code, structure, dependencies, or implemented
features in this repository must also update this file** in the same
session/change so it keeps reflecting the actual state of the project
(files present, endpoints implemented, persistence layer, domain rules,
etc.). Treat a code change that leaves this file stale as incomplete work.

- Small fix with no structural/behavioral impact (typo, formatting) →
  no update needed.
- Anything else that changes what's true about the project (new endpoint,
  new module, new dependency, changed domain rule, new test strategy, etc.)
  → update the relevant section above before finishing the task.
