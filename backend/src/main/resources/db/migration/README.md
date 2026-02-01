# Database migrations (Flyway)

Migrations run **automatically** when the backend starts. No manual steps needed.

**Location:** `src/main/resources/db/migration/`

**Naming:** `V{version}__{description}.sql` (e.g. `V1__create_ai_response.sql`)

**Manual run (optional):** If you need to apply migrations without starting the app, use Flyway CLI or copy the SQL from `V1__create_ai_response.sql` and run against your PostgreSQL database.

**H2 Console (dev):** With default H2 file-based DB, open `http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:file:./data/aletheia`, user `sa`, password empty. Data persists in `backend/data/`.

---

## Liquibase (future)

We may migrate to **Liquibase** later. Reasons to consider:

- **Multi-database support** — XML/YAML changelogs can target PostgreSQL, MySQL, Oracle without separate SQL per DB
- **Rollback support** — Liquibase can generate rollback scripts; Flyway (community) does not
- **Preconditions** — Run migrations only when conditions are met (e.g. table doesn't exist)
- **Team preference** — Some teams standardize on Liquibase for larger projects

For this PoC, Flyway's simplicity (plain SQL, one file per change) was preferred. The `ai_response` schema is stable; if we add more databases or need rollbacks, we can switch.
