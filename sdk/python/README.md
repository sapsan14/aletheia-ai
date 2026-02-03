# Aletheia Python SDK

Minimal Python client for the Aletheia AI API.

## Install (from repo)

```bash
cd sdk/python
pip install .
```

## Usage

```python
import aletheia

# Configure base URL via env or pass base_url explicitly
# export ALETHEIA_API_URL="http://localhost:8080"

signed = aletheia.sign("Hello from my LLM", model_id="external-llm")
record_id = signed["id"]

verify = aletheia.verify(record_id)
print(verify["signatureValid"])

data = aletheia.get_evidence(record_id)
with open("evidence.aep", "wb") as f:
    f.write(data)
```

## Environment variables

- `ALETHEIA_API_URL` — base URL of the backend (e.g. `http://localhost:8080`)

## Publish (manual)

```bash
python -m build
twine upload dist/*
```
