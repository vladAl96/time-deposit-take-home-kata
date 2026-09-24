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

`kotlin/` is a plain Maven project (JDK 17, Kotlin 1.7.20) containing only:

- `src/main/kotlin/org/ikigaidigital/TimeDeposit.kt` — the domain model.
- `src/main/kotlin/org/ikigaidigital/TimeDepositCalculator.kt` — the interest
  engine, entry point `updateBalance(xs: List<TimeDeposit>)`.
- `src/test/kotlin/org/ikigaidigital/TimeDepositCalculatorTest.kt` — a single
  placeholder test that does not actually assert calculator behavior yet.

There is no REST layer, no database, no persistence, no dependency
injection framework, and no build for anything beyond `mvn compile`/`test`
via `exec-maven-plugin`. All of that is greenfield — build it, don't assume
it exists.

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
  `withdrawals.timeDepositId -> timeDeposits.id`.
- **Architecture**: Hexagonal (ports & adapters) — keep
  `TimeDepositCalculator`'s domain logic isolated from web/persistence
  concerns; the two REST endpoints and the DB are adapters around it.
- **API contract**: OpenAPI/Swagger-first.
- **Testing**: testcontainers for anything touching a real database, JUnit 5
  + AssertJ for unit tests (already on the classpath).
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
