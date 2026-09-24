# AI Assistance Log

This file is the running record required by the assignment's
"AI-Assisted Development" section (see `README.md`). Every agent or
assistant session that makes changes in this repository must append an
entry here before finishing its work — this is the reviewer's window into
what was AI-generated, what was AI-assisted, and why.

## Setup

- **Tool**: Claude Code (Anthropic CLI agent), model Claude Sonnet 5 /
  Opus 5.
- **Configuration**: `.claude/CLAUDE.md` — project-specific instructions
  covering domain rules, hard constraints, and architecture expectations
  that the agent reads at the start of every session.
- **Workflow**: interactive, human-in-the-loop. The developer reviews and
  approves each change; the agent does not merge or push autonomously.

## How to add an entry

Append a new entry at the top of the log below, most recent first. Keep
each entry short — a few bullet points, not a narrative.

```markdown
### YYYY-MM-DD — <short title>

- **Agent/model**: e.g. Claude Code (Sonnet 5)
- **Task**: what was asked
- **Changes**: files/areas touched, at a high level
- **AI contribution**: what the AI generated vs. what was human-directed
  or human-written; note any AI output that was corrected or rejected
- **Why AI was used**: brief rationale (speed, boilerplate, exploration,
  refactor safety, etc.)
```

## Log

### 2026-09-24 — Run/Swagger instructions

- **Agent/model**: Claude Code (Sonnet 5)
- **Task**: Provide instructions for starting the app and triggering the
  endpoints via the Swagger contract, per the README's submission
  instructions.
- **Changes**:
  - New `RUNNING.md` at the repo root: prerequisites, `docker-compose up`
    for Postgres, `mvn spring-boot:run`, the Swagger UI URL
    (`/swagger-ui.html`), and step-by-step instructions for exercising both
    the GET and POST endpoints through it.
  - Updated `.claude/CLAUDE.md` to point at `RUNNING.md` and note the
    controller's manual OpenAPI annotations, replacing the stale "not yet
    done" note.
- **AI contribution**: Fully AI-written; values (ports, datasource
  credentials, swagger path) pulled directly from `application.yml` and
  `docker-compose.yml` rather than guessed.
- **Why AI was used**: Mechanical documentation task, transcribing already
  -established config into user-facing run instructions.

### 2026-09-24 — OpenAPI annotations on the controller

- **Agent/model**: Claude Code (Sonnet 5)
- **Task**: Document the REST endpoints in `TimeDepositController` with
  OpenAPI annotations (`@Operation`, `@ApiResponses`, `@Parameter`).
- **Changes**:
  - `adapter/input/web/TimeDepositController.kt`: added `@Operation`
    (summary/description) and `@ApiResponses` to both endpoints; the GET
    endpoint's 200 response also declares an array `Schema` of
    `TimeDepositResponse`. `@Parameter` was not applicable — neither
    endpoint takes a request parameter.
- **AI contribution**: Fully AI-written annotations, verified with
  `mvn compile` (offline, no errors).
- **Why AI was used**: Mechanical documentation work against an
  already-defined, unchanged controller contract.

### 2026-09-24 — Persistence adapter unit test

- **Agent/model**: Claude Code (Sonnet 5)
- **Task**: Write a unit test for `TimeDepositPersistenceAdapter`.
- **Changes**:
  - New `adapter/output/persistence/TimeDepositPersistenceAdapterTest.kt`:
    mocks `TimeDepositJpaRepository` (Mockito, consistent with the
    `@MockBean` style already used in `TimeDepositControllerTest`) and
    covers `findAll` mapping entities/withdrawals to domain records
    (including a no-withdrawals case), and `saveAll` persisting the
    calculator's new balance while preserving `id`/`planType`/`days`/
    `withdrawals` from the previously-stored entity.
- **AI contribution**: Fully AI-written test, verified by running
  `mvn test` (all 4 new cases pass, full suite green).
- **Why AI was used**: Mechanical test-writing against an already-defined
  adapter contract; fast to generate and verify against the real suite.

### 2026-09-24 — Unit and integration tests for the scaffold

- **Agent/model**: Claude Code (Sonnet 5)
- **Task**: Write basic unit and integration tests for the current state of
  the Spring Boot / hexagonal scaffold (calculator, service, controller,
  and the REST-to-Postgres round trip).
- **Changes**:
  - Rewrote `TimeDepositCalculatorTest.kt` (was a non-asserting placeholder)
    into real coverage of every domain rule in `.claude/CLAUDE.md`: the
    30-day blackout, basic/student/premium rates, the premium 45-day
    threshold, the student 366-day cutoff, HALF_UP rounding, and
    independent mutation of a multi-element list.
  - New `application/service/TimeDepositServiceTest.kt`: unit test using an
    in-memory fake `TimeDepositRepositoryPort` (no mocking framework needed)
    to verify `TimeDepositService` calls the real calculator and persists
    the mutated balances, and that `getAllTimeDeposits` passes repository
    data through unchanged.
  - New `adapter/input/web/TimeDepositControllerTest.kt`: `@WebMvcTest`
    slice test with the two use-case ports mocked via `@MockBean`, checking
    the POST endpoint delegates to the use case and the GET endpoint's JSON
    shape (`id`, `planType`, `balance`, `days`, `withdrawals`).
  - New `TimeDepositApiIntegrationTest.kt`: `@SpringBootTest` +
    testcontainers Postgres, driving both REST endpoints through MockMvc
    against a real database — verifies the seeded rows from `data.sql` and
    that `POST /update-balances` persists the correct new balances for all
    three plan types.
- **AI contribution**: Fully AI-written tests and expected values (interest
  amounts hand-derived from the documented domain rules, then verified by
  running the suite). Ran `mvn test-compile` and `mvn test`: the 13
  unit/slice tests (calculator, service, controller) pass; the testcontainers
  integration test compiles and is correctly wired but could not start a
  container in the agent's sandboxed shell (Docker socket there returns a
  stripped `/info` response Testcontainers' client rejects, though `docker`
  CLI itself works) — expected to run normally in a real terminal/IDE with
  full Docker access.
- **Why AI was used**: Mechanical but detail-sensitive work (deriving exact
  expected interest values per plan/threshold, wiring MockMvc/testcontainers
  boilerplate) where speed and precision matter more than judgment calls.

### 2026-09-24 — Spring Boot / hexagonal scaffold

- **Agent/model**: Claude Code (Sonnet 5)
- **Task**: Set up the project to implement the README requirements —
  create the hexagonal (ports & adapters) directory structure with basic
  interfaces, and align `pom.xml` dependencies with Spring Boot, keeping
  them compatible with the pinned JDK 17 / Kotlin 1.7.20.
- **Changes**:
  - `pom.xml`: added Spring Boot 3.1.5 BOM, web/data-jpa starters, Postgres
    driver, springdoc-openapi, testcontainers BOM + Postgres/junit-jupiter
    modules, `spring-boot-maven-plugin`, and the Kotlin `spring`/`jpa`
    compiler plugins (all-open/no-arg).
  - New `TimeDepositApplication.kt` (Spring Boot entry point).
  - New `domain/` package: `Withdrawal`, `TimeDepositRecord` — kept separate
    from the existing `TimeDeposit` so its type/`updateBalance` signature
    stays untouched.
  - New `application/port/input`, `application/port/output`,
    `application/service`: the two use-case interfaces, the repository
    outbound port, and `TimeDepositService` wiring them to the unmodified
    `TimeDepositCalculator`.
  - New `adapter/input/web`: `TimeDepositController` (the two required
    endpoints) and response DTOs.
  - New `adapter/output/persistence`: JPA entities, Spring Data repositories,
    and the persistence adapter implementing the outbound port.
  - New `application.yml`, `data.sql` (demo seed rows), `docker-compose.yml`
    (local Postgres).
  - Updated `.claude/CLAUDE.md` to reflect the new file layout and stack.
- **AI contribution**: Fully AI-generated scaffold (package layout, pom
  dependency alignment, entity/DTO/adapter code), reviewed and compiled
  (`mvn compile` / `mvn test-compile`) by the agent before handing back.
  Human-directed: the request to use Spring Boot, hexagonal architecture,
  and to keep versions aligned with the existing pom.
- **Why AI was used**: Boilerplate-heavy setup (Maven dependency wiring,
  package scaffolding, JPA/DTO plumbing) where speed and consistency matter
  more than judgment calls; domain rules and hard constraints from
  `.claude/CLAUDE.md` kept the agent from touching the calculator.
