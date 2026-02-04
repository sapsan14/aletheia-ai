# Ansible Deploy — Aletheia AI

Deploys the full stack (PostgreSQL, backend, frontend) to a target VM via Docker. **Verified** on Ubuntu 22.04 with Docker Compose v2.

## Prerequisites

- Ansible 2.14+
- SSH access to target VM (e.g. `ssh ubuntu@193.40.157.132`)
- Signing key `ai.key` in project root (or set `signing_key_src`)

## Quick run

From project root:

```bash
# 1. Ensure ai.key exists on your machine (required for backend)
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048

# 2. Deploy (default host: inventory.yml)
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml
```

**Result:** Postgres, backend, frontend run at `/opt/aletheia-ai`. Frontend is bound to host port **3001** (to avoid conflicts with other users on the same machine). With ngrok: `https://<ngrok_domain>`; locally: `http://VM:3001`. Backend is not exposed on the host (only via frontend proxy).

## Update frontend only

To update only the frontend on the remote (pull latest code, rebuild and restart the frontend container; no .env, keys, or backend changes):

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml --tags frontend
```

Requires the app to be already deployed (Docker and repo at `/opt/aletheia-ai`).

## With secrets

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml \
  -e postgres_password=YOUR_SECURE_PASSWORD \
  -e openai_api_key=sk-your-openai-key
```

For production, set the frontend API URL (browser will call this):
```bash
-e next_public_api_url=http://YOUR_VM_IP:8080
```

## CI/CD (GitHub Actions)

Push to `main` triggers automated deploy. Configure GitHub Secrets (Settings → Secrets and variables → Actions):

- **Required:** `DEPLOY_HOST`, `DEPLOY_USER`, `SSH_PRIVATE_KEY`, `SIGNING_KEY` (PEM content of `ai.key`)
- **Optional:** `POSTGRES_PASSWORD`, `OPENAI_API_KEY`, `NEXT_PUBLIC_API_URL`

See [.github/workflows/deploy.yml](../../.github/workflows/deploy.yml) and main [README Deployment section](../../README.md#github-actions-cicd).

## Override target host

Edit `inventory.yml` or create a custom inventory. To override via CLI:

```bash
ansible-playbook -i 'aletheia ansible_host=YOUR_VM_IP,' deploy/ansible/playbook.yml
```

## Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `deploy_repo_url` | https://github.com/sapsan14/aletheia-ai.git | Repo to clone |
| `deploy_repo_version` | main | Branch or tag |
| `postgres_password` | local | DB password |
| `openai_api_key` | (empty) | OpenAI API key. **Redeploy:** if you don't pass `-e openai_api_key`, the playbook keeps the existing value from server `.env` (no overwrite). |
| `postgres_db` / `postgres_user` / `postgres_password` | aletheia / aletheia / local | DB name, user, password. **Redeploy:** if you don't pass them, the playbook **preserves** existing values from server `.env`, so the Postgres password is never overwritten and backend keeps matching the DB. |
| `cors_allowed_origins` | http://localhost:3000 | CORS allowed origins (comma-separated). **ngrok:** add `https://your-subdomain.ngrok-free.dev` |
| `next_public_api_url` | http://localhost:8080 | **ngrok:** leave empty (playbook sets it); frontend uses relative `/api`, proxied by Next.js. **Production (no ngrok):** `http://YOUR_VM_IP:8080` |
| `deploy_app_host_port` | **3001** | Host port for frontend (avoids 3000 so other users can use it). ngrok tunnels this port. |
| `deploy_backend_port` | **8081** | Internal port the backend listens on (avoids 8080 so other users can use it). Frontend proxy uses `http://backend:8081`. |
| `deploy_compose_project` | **aletheia-ai** | Docker Compose project name (isolates our stack from other users' containers). |
| `ngrok_enabled` | **true** | Single-port via ngrok; API proxied by Next.js. Set `false` to expose VM directly. |
| `ngrok_authtoken` | — | **Required** when ngrok_enabled. From https://dashboard.ngrok.com/get-started/your-authtoken |
| `ngrok_domain` | kaia-uncharacterized-unorbitally.ngrok-free.dev | ngrok free domain for tunnel |
| `signing_key_src` | `{{ playbook_dir }}/../../ai.key` | Override path to PEM key (`-e signing_key_src=/path/to/ai.key`) |
| `pqc_key_src` | `{{ playbook_dir }}/../../backend/ai_pqc.key` | Optional path to PQC (ML-DSA) private key; if present, copied to server as `ai_pqc.key` and PQC enabled in `.env` |
| `ai_aletheia_pqc_enabled` | (set when PQC key copied) | Set automatically when `pqc_key_src` exists; or override with `-e ai_aletheia_pqc_enabled=true` |
| `ai_aletheia_pqc_key_path` | (set when PQC key copied) | Set to `/app/ai_pqc.key` in container when PQC key is copied |

**PQC (post-quantum) on the server:** To enable ML-DSA signing on deploy, either (1) **copy key from your machine:** generate locally (`cd backend && mvn -q exec:java -Dexec.mainClass="ai.aletheia.crypto.PqcKeyGen" -Dexec.args="."`), then run the playbook with `-e pqc_key_src=/path/to/backend/ai_pqc.key` (or leave default `backend/ai_pqc.key` if that file exists); or (2) **generate on the server after deploy (no Maven needed):** SSH to the VM, then from `/opt/aletheia-ai` run the key generator inside the backend container:

```bash
cd /opt/aletheia-ai
docker compose -p aletheia-ai run --rm -v /opt/aletheia-ai:/out backend java -cp /app/app.jar ai.aletheia.crypto.PqcKeyGen /out
```

This creates `ai_pqc.key` and `ai_pqc_public.pem` in `/opt/aletheia-ai`. Then add to `.env`: `AI_ALETHEIA_PQC_ENABLED=true` and `AI_ALETHEIA_PQC_KEY_PATH=/app/ai_pqc.key`, ensure `docker-compose.override.yml` has backend volume `./ai_pqc.key:/app/ai_pqc.key:ro` (playbook does this when PQC key exists), and run `docker compose -p aletheia-ai up -d --force-recreate backend`. See [docs/internal/en/plan-pqc.md](../../docs/internal/en/plan-pqc.md) for key generation and configuration.

All `.env.j2` template variables can be overridden via `-e`.

### Example: ngrok + CORS

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml \
  -e "cors_allowed_origins=https://kaia-uncharacterized-unorbitally.ngrok-free.dev,http://localhost:3000"
```

### ngrok auto-start (systemd)

Expose the frontend via ngrok and start it on boot.

**Option 1 — use .env:** Add to your project root `.env`:
```
NGROK_AUTHTOKEN=your_token_here
```

Then run:
```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml -e ngrok_enabled=true
```

**Option 2 — pass via -e:**
```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml \
  -e ngrok_enabled=true -e ngrok_authtoken=YOUR_NGROK_AUTHTOKEN
```

Get your authtoken at https://dashboard.ngrok.com/get-started/your-authtoken

| Variable | Default | Description |
|----------|---------|-------------|
| `ngrok_enabled` | **true** | Install and run ngrok as systemd service (port 3000). Set `-e ngrok_enabled=false` to disable. |
| `ngrok_authtoken` | — | **Required** when ngrok_enabled. From ngrok dashboard |
| `ngrok_domain` | kaia-uncharacterized-unorbitally.ngrok-free.dev | Your ngrok free domain |
| `ngrok_port` | same as `app_host_port` (default 3001) | Local port to tunnel (our frontend). |

The service runs `ngrok http <app_host_port> --domain=<ngrok_domain>` (direct CLI). Default port is **3001** so our app does not conflict with other users' apps on port 3000. Authtoken from `NGROK_AUTHTOKEN` in `.env` or `/etc/ngrok/ngrok.env`. Ensure CORS includes your ngrok URL (see above).

**Full ngrok (one command):** Exposes app via single tunnel (free plan = 1 endpoint). API calls go through the [Next.js runtime proxy](frontend/app/api/[...path]/route.ts) (same origin → no CORS).

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml -e ngrok_enabled=true
```

Add `NGROK_AUTHTOKEN` to `.env` (project root). The playbook will:
- Bind **only port 3001** on the host for our frontend (postgres and backend stay internal). Backend listens on **8081** inside the stack (not on host), so 3000 and 8080 stay free for other users.
- Use Docker Compose project name `aletheia-ai` so our stack is isolated from other users' containers
- Start ngrok tunnel for frontend (port 3001, your free domain)
- Set `NEXT_PUBLIC_API_URL=` so the client uses relative `/api` URLs (proxied to backend by [frontend/app/api/[...path]/route.ts](frontend/app/api/[...path]/route.ts))
- Rebuild frontend with empty API URL, set CORS on backend

### Shared machine (multiple users / no port conflicts)

If several people run apps on the same VM (e.g. university server), use the default settings: our app uses **frontend port 3001** and **backend internal port 8081** (not exposed on host). Others can use 3000 and 8080 and their own compose project. To use a different port:

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml -e deploy_app_host_port=3002
```

Then on the VM, fix ngrok to point to 3002: edit `/etc/systemd/system/ngrok.service`, set `ExecStart=... http 3002 --domain=...`, then `sudo systemctl daemon-reload && sudo systemctl restart ngrok`.

## API proxy (Docker)

In Docker Compose the frontend talks to the backend via a **runtime proxy**: requests to `/api/*` are handled by [frontend/app/api/[...path]/route.ts](frontend/app/api/[...path]/route.ts), which reads `BACKEND_INTERNAL_URL` from the container env (set to `http://backend:8080` in [docker-compose.yml](../../docker-compose.yml)) and forwards each request to the backend. No build-time `BACKEND_INTERNAL_URL` is required. For ngrok, set `NEXT_PUBLIC_API_URL=` so the client uses relative `/api` URLs (same origin); the proxy then forwards to the backend. See [CORS when opening app via ngrok](#cors-when-opening-app-via-ngrok-fetch-to-localhost8080-blocked).

## Idempotency

Running the playbook again is safe: it updates the repo, recreates `.env`, and runs `docker compose up -d --build` (which recreates only changed containers).

## Verified deployment flow

The playbook has been tested end-to-end. Typical run time: ~30 s (no rebuild) to ~3 min (full rebuild).

| Step | Ansible task | Result |
|------|--------------|--------|
| 1 | Install Docker | docker-ce, docker-compose-plugin |
| 2 | Clone/update repo | `/opt/aletheia-ai` |
| 3 | Template `.env` | From `templates/.env.j2` |
| 4 | Copy `ai.key` | From control node (if present) |
| 5 | `docker compose up -d --build` | postgres, backend, frontend |

**Fresh VM:** Ensure `ai.key` exists on the machine running Ansible; playbook copies it. If key is missing, playbook fails with a clear message.

**Re-deploy:** Safe to run again; updates repo, `.env`, and restarts containers. Existing `ai.key` on server is kept.

---

## Troubleshooting

### Backend exits on start (Exited 1)

If `docker ps -a` shows `aletheia-backend` as **Exited (1)**, the app failed during startup. **On the VM:**

```bash
cd /opt/aletheia-ai
sudo docker compose -p aletheia-ai logs --tail=80 backend
```

Fix the cause (see below), then **force-recreate** so the container restarts with the same or updated config:

```bash
sudo docker compose -p aletheia-ai up -d --force-recreate backend
```

After changing `.env`, always run the same `up -d --force-recreate backend` so the backend picks up new env.

### 500 when sending a request (e.g. Send & Verify)

The frontend proxies `/api/*` to the backend. A **500** usually means the backend threw an exception (or the backend container is down and the proxy returns 500). **On the VM:**

```bash
cd /opt/aletheia-ai
docker compose -p aletheia-ai logs --tail=100 backend
```

**Typical causes:**

1. **Missing or invalid OPENAI_API_KEY** — Backend needs it for `/api/ai/ask`. Set in `/opt/aletheia-ai/.env` and recreate backend: `docker compose -p aletheia-ai up -d --force-recreate backend`.
2. **SpEL / OPENAI_API_KEY** — If logs show `SpelParseException: Unexpected escape character` and the expression contains your API key, the backend was built with a condition that embeds the key in a SpEL expression (fixed in current code). **Fix:** Rebuild and redeploy the backend image, or ensure you use a commit that has the fix (condition uses `@environment.getProperty(...)` instead of `${...}` in the expression).
3. **Signing key** — If you see "Signing key file not found" or "Could not load private key", fix `ai.key` (see [ai.key: "Is a directory"](#aikey-is-a-directory-or-signing-key-file-not-found)).
4. **Database** — If Postgres was down or schema failed, check `docker compose -p aletheia-ai logs postgres` and ensure backend can reach `postgres:5432` (internal network). If you see "password authentication failed for user \"aletheia\"", see [Postgres password mismatch](#postgres-password-authentication-failed-for-user-aletheia).
5. **TSA / timestamping** — Real TSA may be unreachable; try `AI_ALETHEIA_TSA_MODE=mock` in `.env` for testing.

After changing `.env`, always **recreate** the backend container so it picks up new env: `docker compose -p aletheia-ai up -d --force-recreate backend`.

### Postgres: password authentication failed for user "aletheia"

**Symptom:** Backend logs: `FATAL: password authentication failed for user "aletheia"`.

**Cause:** Postgres sets the user/password **only on first startup** (when the data volume is empty). After that, the password is fixed in the volume. Previously, the playbook **overwrote** `.env` with template defaults on every run (e.g. `POSTGRES_PASSWORD=local` when you didn't pass `-e postgres_password`). So the second deploy overwrote whatever password was in `.env` with the default; the backend then used the new value while Postgres still had the old one → auth failed. **Now the playbook preserves existing `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD` from the server `.env` when you don't pass them**, so this should not happen again. If you already hit the error, use one of the fixes below.

**Fix (keep existing DB data):** Edit `.env` on the server so that **user** and **password** match what Postgres was created with:

1. **Typo:** If the error says user **"alethia"** (one e), fix it to **aletheia** (two e's):  
   `POSTGRES_USER=aletheia`
2. **Password:** Use the password that was in `.env` when the Postgres container was **first** started. If you didn’t set one, the playbook default was `local` — try `POSTGRES_PASSWORD=local`.

```bash
sudo nano /opt/aletheia-ai/.env
# Ensure exactly (no typos):
#   POSTGRES_USER=aletheia
#   POSTGRES_PASSWORD=local
# (or the password you first used on the server)
sudo docker compose -p aletheia-ai up -d --force-recreate backend
```

**Fix (reset DB, lose data):** If you don’t remember the old password or want a clean DB:

```bash
cd /opt/aletheia-ai
sudo docker compose -p aletheia-ai down
sudo docker volume rm aletheia-ai_postgres_data    # or: docker volume ls then rm the aletheia postgres volume
# Edit .env: set POSTGRES_PASSWORD to the value you want (e.g. local or a new secret)
sudo docker compose -p aletheia-ai up -d
```

Then recreate the backend so it uses the same `.env`: `sudo docker compose -p aletheia-ai up -d --force-recreate backend`.

### Only postgres running

If `docker ps` shows only `aletheia-db`, backend/frontend may have failed. **On the VM:**

```bash
cd /opt/aletheia-ai
docker compose -p aletheia-ai ps -a          # All containers including exited
docker compose -p aletheia-ai logs backend   # Backend startup errors
docker compose -p aletheia-ai logs frontend  # Frontend build/start errors
docker compose -p aletheia-ai up --build     # Run in foreground to see build output
```

### ai.key: "Is a directory" or "Signing key file not found"

**Cause:** If `ai.key` did not exist when docker-compose first ran, Docker created `ai.key` as an **empty directory** (volume mount quirk). Backend then fails: "Signing key file not found: /app/ai.key".

**Fix on the VM:**

```bash
cd /opt/aletheia-ai
sudo rm -rf ai.key                                    # Remove the directory
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
chmod 600 ai.key
docker compose -p aletheia-ai down && docker compose -p aletheia-ai up -d   # Recreate containers
```

Or copy a key from your machine: `scp ai.key ubuntu@VM:/opt/aletheia-ai/ai.key`

### Frontend exited: "Cannot find module 'typescript'"

**Cause:** `next.config.ts` required TypeScript at runtime; production image uses `npm ci --omit=dev` (no devDependencies).

**Fix:** Resolved by using `next.config.mjs` (plain JS). Ensure you have the latest commit. Rebuild: `docker compose -p aletheia-ai build frontend --no-cache && docker compose -p aletheia-ai up -d`.

### ngrok: status=203/EXEC or "ngrok unavailable"

**Cause:** systemd cannot execute ngrok — wrong path. ngrok may be at `/usr/local/bin/ngrok` (manual install) instead of `/usr/bin/ngrok`.

**Fix:** Re-run the playbook (auto-detects path), or manually `sudo sed -i 's|/usr/bin/ngrok|/usr/local/bin/ngrok|g' /etc/systemd/system/ngrok.service` then `sudo systemctl daemon-reload && sudo systemctl restart ngrok`.

### ngrok: shows help and exits (inactive/dead)

**Cause:** Old `ngrok start --config` syntax may fail with some ngrok versions (v2 vs v3).

**Fix:** The playbook now uses `ngrok http PORT --domain=DOMAIN` (direct CLI). Re-run the playbook, or on the VM replace the unit file with the correct content (see [Broken ExecStart](#ngrok-broken-execstart-multiple-lines) below).

### ngrok: broken ExecStart (multiple lines)

**Cause:** The unit file has a split or malformed `ExecStart` (e.g. path on one line, full command on another, or extra lines like `/usr/bin/ngrok`). systemd then runs only the first token and ngrok exits.

**Fix:** On the VM, replace the unit file with exactly this (one line for `ExecStart`; adjust domain if you use another):

```bash
sudo tee /etc/systemd/system/ngrok.service << 'EOF'
[Unit]
Description=ngrok tunnel for Aletheia frontend
After=network-online.target docker.service
Wants=network-online.target

[Service]
Type=simple
EnvironmentFile=/etc/ngrok/ngrok.env
ExecStart=/usr/local/bin/ngrok http 3001 --domain=kaia-uncharacterized-unorbitally.ngrok-free.dev
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
sudo systemctl daemon-reload
sudo systemctl restart ngrok
sudo systemctl status ngrok
```

If ngrok is at `/usr/bin/ngrok`, change the `ExecStart` line to use that path. Ensure `/etc/ngrok/ngrok.env` exists and contains `NGROK_AUTHTOKEN=your_token`.

### ngrok: inactive (dead) — OAuth / paid features

**Cause:** The service was edited to use `--oauth=google` or `--oauth-allow-email=...`. Those are **paid-only** features; on the free plan ngrok prints "Upgrade your account" and exits, so the unit becomes inactive (dead).

**Fix:** Use the free-tier command only (no OAuth). Either re-run the playbook so it overwrites the unit:

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml -e ngrok_enabled=true -e ngrok_authtoken=YOUR_TOKEN
```

Or on the VM, edit the service and remove OAuth flags:

```bash
sudo nano /etc/systemd/system/ngrok.service
# ExecStart must be (replace with your domain and port):
#   ExecStart=/usr/local/bin/ngrok http 3001 --domain=YOUR-DOMAIN.ngrok-free.dev
# Remove any --oauth=... or --oauth-allow-email=...
sudo systemctl daemon-reload
sudo systemctl restart ngrok
sudo systemctl status ngrok
```

### CORS when opening app via ngrok (fetch to localhost:8080 blocked)

**Symptom:** Browser console shows: `Access to fetch at 'http://localhost:8080/api/ai/ask' from origin 'https://...ngrok-free.dev' has been blocked by CORS policy`.

**Cause:** The frontend was built with `NEXT_PUBLIC_API_URL=http://localhost:8080`. The client then calls that URL from the browser; when you open the app via the ngrok URL, the browser sends the request to *your* machine’s localhost, not the server, and the backend (or lack of it) doesn’t allow the ngrok origin.

**Fix:** Use relative `/api` URLs so the browser talks only to the ngrok origin; the [Next.js runtime proxy](frontend/app/api/[...path]/route.ts) forwards `/api/*` to the backend. Either run the playbook with `-e ngrok_enabled=true` (it sets `NEXT_PUBLIC_API_URL=` and rebuilds), or on the VM:

```bash
sed -i 's|^NEXT_PUBLIC_API_URL=.*|NEXT_PUBLIC_API_URL=|' .env
docker compose -p aletheia-ai build frontend --no-cache --build-arg NEXT_PUBLIC_API_URL=
docker compose -p aletheia-ai up -d
```

### University network / firewall — use ngrok

If VM ports (3000, 8080) are blocked from outside (e.g. university network), expose via ngrok. **Single command** (free plan = 1 endpoint):

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml -e ngrok_enabled=true
```

Add `NGROK_AUTHTOKEN` to `.env`. The playbook starts one tunnel (frontend on port 3001 by default), sets `NEXT_PUBLIC_API_URL=` and rebuilds so the client uses relative `/api` URLs. The [Next.js runtime proxy](frontend/app/api/[...path]/route.ts) forwards `/api/*` to the backend at `BACKEND_INTERNAL_URL` (set to `http://backend:8080` in docker-compose). No second tunnel or CORS needed.

### Changing OPENAI_API_KEY or Postgres vars on the server

**Redeploy and .env:** The playbook rewrites `.env` from a template on each run. When you **do not** pass a variable, it **preserves** the existing value from the server's current `.env`:

- **OPENAI_API_KEY** — Pass `-e openai_api_key=sk-...` only when you want to set or change it; otherwise the existing key is kept.
- **POSTGRES_DB / POSTGRES_USER / POSTGRES_PASSWORD** — Pass `-e postgres_password=...` (and optionally `postgres_db` / `postgres_user`) only when you want to set or change them; otherwise the existing values are kept. This avoids overwriting the DB password on redeploy and prevents "password authentication failed" (Postgres keeps the password it was first created with).

**Symptom:** You set `OPENAI_API_KEY=sk-...` in `/opt/aletheia-ai/.env` and ran `docker compose restart backend`, but the app still shows "Set OPENAI_API_KEY on the server...".

**Cause:** `docker compose restart` only stops and starts the same container. Environment variables are set when the container is **created**, not when it restarts. The backend container was created with the old (empty) `.env`.

**Fix on the VM:** Recreate the backend container so it picks up the new `.env`:

```bash
cd /opt/aletheia-ai
docker compose -p aletheia-ai up -d --force-recreate backend
```

After any change to `.env` that affects a service, use `--force-recreate` for that service (or `docker compose -p aletheia-ai down && docker compose -p aletheia-ai up -d`) so the new variables are applied.

**Note:** `OPENAI_API_KEY` is the OpenAI API key string (from https://platform.openai.com/api-keys). The signing key file `ai.key` is configured separately via docker-compose (mounted at `/app/ai.key`); do not set `OPENAI_API_KEY=./ai.key`.

**Correct format in `.env`:** One line, no quotes or brackets around the key:

```bash
OPENAI_API_KEY=sk-proj-xxxxxxxxxxxx
```

If you pass the key via Ansible, use the **plain value** only: `-e openai_api_key=sk-proj-...` (no `["..."]` or `'...'` around the key). Otherwise the key can be written with extra quoting; the playbook then "preserves" that corrupted value on every redeploy.

### OPENAI_API_KEY looks corrupted (nested quotes/backslashes)

**Symptom:** In `/opt/aletheia-ai/.env` the line looks like:

```bash
OPENAI_API_KEY=['[\'[\\\'[\\\\\\\'["[\\\\...sk-proj-...\\\\...\']"]...
```

**Cause:** The key was set once with extra quoting (e.g. as a YAML list or quoted string passed to `-e openai_api_key=...`). The playbook’s "preserve existing OPENAI_API_KEY" logic then keeps re-writing that same corrupted value on every deploy.

**Fix on the VM:** Edit `.env` and replace the value with **only** the key (no quotes, no brackets):

```bash
sudo nano /opt/aletheia-ai/.env
# Change the OPENAI_API_KEY line to exactly:
# OPENAI_API_KEY=sk-proj-your-actual-key-here
# (plain key only; no quotes or brackets)
```

Save, then recreate the backend so it picks up the fixed env:

```bash
sudo docker compose -p aletheia-ai up -d --force-recreate backend
```

From now on, redeploys will preserve this correct value as long as you don’t pass `-e openai_api_key=...` with quoted/list syntax.

### Other causes

- **ai.key missing on control node** — Playbook skips copy; fails at "Fail if signing key is missing". Create key or pass `-e signing_key_src=/path/to/ai.key`.
- **Build failure** — Maven/npm may fail (network, memory). Run `docker compose -p aletheia-ai up --build` to see full output.
