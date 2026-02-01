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

**Result:** Postgres, backend, frontend run at `/opt/aletheia-ai`. Frontend: `http://VM:3000`, Backend: `http://VM:8080`.

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
| `openai_api_key` | (empty) | OpenAI API key |
| `cors_allowed_origins` | http://localhost:3000 | CORS allowed origins (comma-separated). **ngrok:** add `https://your-subdomain.ngrok-free.dev` |
| `next_public_api_url` | http://localhost:8080 | **ngrok:** leave empty (playbook sets it); frontend uses relative `/api`, proxied by Next.js. **Production (no ngrok):** `http://YOUR_VM_IP:8080` |
| `ngrok_enabled` | false | Set `true` to install ngrok and run as systemd service (auto-start on boot) |
| `ngrok_authtoken` | — | **Required** when ngrok_enabled. From https://dashboard.ngrok.com/get-started/your-authtoken |
| `ngrok_domain` | kaia-uncharacterized-unorbitally.ngrok-free.dev | ngrok free domain for tunnel |
| `signing_key_src` | `{{ playbook_dir }}/../../ai.key` | Override path to PEM key (`-e signing_key_src=/path/to/ai.key`) |

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
| `ngrok_enabled` | false | Set to `true` to install and run ngrok as systemd service |
| `ngrok_authtoken` | — | **Required** when ngrok_enabled. From ngrok dashboard |
| `ngrok_domain` | kaia-uncharacterized-unorbitally.ngrok-free.dev | Your ngrok free domain |
| `ngrok_port` | 3000 | Local port to tunnel (frontend) |

The service runs `ngrok http 3000 --domain=<ngrok_domain>` (direct CLI, not config file) and restarts on failure. Authtoken from `NGROK_AUTHTOKEN` in `.env` or `/etc/ngrok/ngrok.env`. Ensure CORS includes your ngrok URL (see above).

**Full ngrok (one command):** Exposes app via single tunnel (free plan = 1 endpoint). API calls go through the [Next.js runtime proxy](frontend/app/api/[...path]/route.ts) (same origin → no CORS).

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml -e ngrok_enabled=true
```

Add `NGROK_AUTHTOKEN` to `.env` (project root). The playbook will:
- Start ngrok tunnel for frontend (port 3000, your free domain)
- Set `NEXT_PUBLIC_API_URL=` so the client uses relative `/api` URLs (proxied to backend by [frontend/app/api/[...path]/route.ts](frontend/app/api/[...path]/route.ts))
- Rebuild frontend with empty API URL, set CORS on backend

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

### Only postgres running

If `docker ps` shows only `aletheia-db`, backend/frontend may have failed. **On the VM:**

```bash
cd /opt/aletheia-ai
docker compose ps -a          # All containers including exited
docker compose logs backend   # Backend startup errors
docker compose logs frontend  # Frontend build/start errors
docker compose up --build     # Run in foreground to see build output
```

### ai.key: "Is a directory" or "Signing key file not found"

**Cause:** If `ai.key` did not exist when docker-compose first ran, Docker created `ai.key` as an **empty directory** (volume mount quirk). Backend then fails: "Signing key file not found: /app/ai.key".

**Fix on the VM:**

```bash
cd /opt/aletheia-ai
sudo rm -rf ai.key                                    # Remove the directory
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048
chmod 600 ai.key
docker compose down && docker compose up -d           # Recreate containers
```

Or copy a key from your machine: `scp ai.key ubuntu@VM:/opt/aletheia-ai/ai.key`

### Frontend exited: "Cannot find module 'typescript'"

**Cause:** `next.config.ts` required TypeScript at runtime; production image uses `npm ci --omit=dev` (no devDependencies).

**Fix:** Resolved by using `next.config.mjs` (plain JS). Ensure you have the latest commit. Rebuild: `docker compose build frontend --no-cache && docker compose up -d`.

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
ExecStart=/usr/local/bin/ngrok http 3000 --domain=kaia-uncharacterized-unorbitally.ngrok-free.dev
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
#   ExecStart=/usr/local/bin/ngrok http 3000 --domain=YOUR-DOMAIN.ngrok-free.dev
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
docker compose build frontend --no-cache --build-arg NEXT_PUBLIC_API_URL=
docker compose up -d
```

### University network / firewall — use ngrok

If VM ports (3000, 8080) are blocked from outside (e.g. university network), expose via ngrok. **Single command** (free plan = 1 endpoint):

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml -e ngrok_enabled=true
```

Add `NGROK_AUTHTOKEN` to `.env`. The playbook starts one tunnel (frontend on port 3000), sets `NEXT_PUBLIC_API_URL=` and rebuilds so the client uses relative `/api` URLs. The [Next.js runtime proxy](frontend/app/api/[...path]/route.ts) forwards `/api/*` to the backend at `BACKEND_INTERNAL_URL` (set to `http://backend:8080` in docker-compose). No second tunnel or CORS needed.

### Other causes

- **ai.key missing on control node** — Playbook skips copy; fails at "Fail if signing key is missing". Create key or pass `-e signing_key_src=/path/to/ai.key`.
- **Build failure** — Maven/npm may fail (network, memory). Run `docker compose up --build` to see full output.
