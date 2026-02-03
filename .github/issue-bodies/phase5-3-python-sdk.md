Minimal Python client: sign(), verify(), get_evidence(); publishable to PyPI.

**Acceptance Criteria:**
- Python package with: `aletheia.sign(text, model_id=..., policy_id=...)`, `aletheia.verify(id)`, `aletheia.get_evidence(id)` (or download to path)
- Uses ALETHEIA_API_URL or base_url parameter
- Package builds (`python -m build`); documented for PyPI publish
- Unit tests (mocked HTTP) for sign/verify/get_evidence

См. `docs/en/PLAN_PHASE5.md` — 5.3, Tasks P5.3.1, P5.3.2.
