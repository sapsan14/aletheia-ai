import json
from typing import Any, Optional, Union

import requests


class AletheiaAPIError(Exception):
    """Raised when the API returns a non-2xx response."""

    def __init__(self, status_code: int, message: str, payload: Optional[Any] = None) -> None:
        super().__init__(f"Aletheia API error ({status_code}): {message}")
        self.status_code = status_code
        self.message = message
        self.payload = payload


class AletheiaClient:
    def __init__(self, base_url: str, timeout: float = 30.0, session: Optional[requests.Session] = None):
        if not base_url:
            raise ValueError("base_url is required")
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self._session = session or requests.Session()

    def sign(
        self,
        response: str,
        model_id: Optional[str] = None,
        policy_id: Optional[str] = None,
        prompt: Optional[str] = None,
        request_id: Optional[str] = None,
    ) -> dict:
        payload = {"response": response}
        if model_id:
            payload["modelId"] = model_id
        if policy_id:
            payload["policyId"] = policy_id
        if prompt:
            payload["prompt"] = prompt
        if request_id:
            payload["requestId"] = request_id
        return self._request_json("POST", "/api/sign", json_body=payload)

    def verify(self, record_id: Union[int, str]) -> dict:
        return self._request_json("GET", f"/api/ai/verify/{record_id}")

    def get_evidence(self, record_id: Union[int, str]) -> bytes:
        return self._request_bytes("GET", f"/api/ai/evidence/{record_id}")

    def get_evidence_path(self, record_id: Union[int, str], path: str) -> str:
        data = self.get_evidence(record_id)
        with open(path, "wb") as handle:
            handle.write(data)
        return path

    def _request_json(self, method: str, path: str, json_body: Optional[dict] = None) -> dict:
        response = self._session.request(
            method,
            f"{self.base_url}{path}",
            json=json_body,
            timeout=self.timeout,
        )
        if not response.ok:
            raise self._build_error(response)
        try:
            return response.json()
        except json.JSONDecodeError:
            raise AletheiaAPIError(response.status_code, "Invalid JSON response")

    def _request_bytes(self, method: str, path: str) -> bytes:
        response = self._session.request(
            method,
            f"{self.base_url}{path}",
            timeout=self.timeout,
        )
        if not response.ok:
            raise self._build_error(response)
        return response.content

    def _build_error(self, response: requests.Response) -> AletheiaAPIError:
        message = response.text or "Unknown error"
        payload: Optional[Any] = None
        try:
            payload = response.json()
            if isinstance(payload, dict):
                for key in ("message", "error", "code", "details"):
                    if key in payload and payload[key]:
                        message = str(payload[key])
                        break
        except json.JSONDecodeError:
            payload = None
        return AletheiaAPIError(response.status_code, message, payload)
