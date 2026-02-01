# Ansible Deploy — Aletheia AI

Deploys the full stack (PostgreSQL, backend, frontend) to a target VM via Docker.

## Prerequisites

- Ansible 2.14+
- SSH access to target VM (e.g. `ssh ubuntu@193.40.157.132`)
- Signing key `ai.key` in project root (or set `signing_key_src`)

## Quick run

From project root:

```bash
# Ensure ai.key exists (generate if needed)
openssl genpkey -algorithm RSA -out ai.key -pkeyopt rsa_keygen_bits:2048

# Deploy (default host: 193.40.157.132)
ansible-playbook -i deploy/ansible/inventory.yml deploy/ansible/playbook.yml
```

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

## Override target host

Edit `inventory.yml` or create a custom inventory. To override via CLI:

```bash
ansible-playbook -i 'aletheia ansible_host=YOUR_VM_IP,' deploy/ansible/playbook.yml
```

## Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `deploy_host` | 193.40.157.132 | Target VM IP |
| `deploy_repo_url` | https://github.com/sapsan14/aletheia-ai.git | Repo to clone |
| `deploy_repo_version` | main | Branch or tag |
| `postgres_password` | local | DB password |
| `openai_api_key` | (empty) | OpenAI API key |
| `signing_key_src` | `{{ playbook_dir }}/../../ai.key` | Override local path to PEM key (`-e signing_key_src=/path/to/ai.key`) |

All `.env.j2` template variables can be overridden via `-e`.

## Idempotency

Running the playbook again is safe: it updates the repo, recreates `.env`, and runs `docker compose up -d --build` (which recreates only changed containers).

## Troubleshooting: only postgres running

If `docker ps` shows only `aletheia-db` (postgres), backend/frontend may have failed to build or exited. **On the VM** run:

```bash
cd /opt/aletheia-ai
docker compose ps -a          # All containers including exited
docker compose logs backend   # Backend startup errors
docker compose logs frontend  # Frontend build/start errors
docker compose up --build     # Run in foreground to see build output
```

Common causes:
- **ai.key missing** — Ensure `ai.key` exists before running the playbook; or copy it manually to `/opt/aletheia-ai/ai.key`.
- **Build failure** — Maven/npm may fail (network, memory). Run `docker compose up --build` to see full output.
- **Backend exits** — Check `docker compose logs backend` for DB connection, TSA, or signing key errors.
