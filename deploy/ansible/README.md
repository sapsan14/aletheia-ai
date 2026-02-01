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
| `next_public_api_url` | http://localhost:8080 | **Production:** set to `http://YOUR_VM_IP:8080` or backend ngrok URL so frontend calls correct backend |
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

The service runs `ngrok http 3000 --domain=<ngrok_domain>` and restarts on failure. Ensure CORS includes your ngrok URL (see above).

**Full example (ngrok + CORS):**

```bash
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml \
  -e ngrok_enabled=true \
  -e "cors_allowed_origins=https://kaia-uncharacterized-unorbitally.ngrok-free.dev,http://localhost:3000"
```

Add `NGROK_AUTHTOKEN` to `.env` or pass `-e ngrok_authtoken=xxx`.

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

**Fix on the VM:**

```bash
which ngrok   # e.g. /usr/local/bin/ngrok
sudo sed -i 's|/usr/bin/ngrok|/usr/local/bin/ngrok|g' /etc/systemd/system/ngrok.service
sudo systemctl daemon-reload
sudo systemctl restart ngrok
```

The playbook auto-detects the path; re-run it to fix the service file.

### University network / firewall — use ngrok

If VM ports (3000, 8080) are blocked from outside (e.g. university network), expose via ngrok:

```bash
# On VM: ngrok tunnels localhost:3000 to a public URL
ansible-playbook ... -e ngrok_enabled=true -e "cors_allowed_origins=https://YOUR-NGROK-URL.ngrok-free.dev,http://localhost:3000"
```

Frontend: https://YOUR-NGROK-URL.ngrok-free.dev. Backend needs a second ngrok tunnel and frontend rebuild with that URL (see CORS section above).

### Other causes

- **ai.key missing on control node** — Playbook skips copy; fails at "Fail if signing key is missing". Create key or pass `-e signing_key_src=/path/to/ai.key`.
- **Build failure** — Maven/npm may fail (network, memory). Run `docker compose up --build` to see full output.
