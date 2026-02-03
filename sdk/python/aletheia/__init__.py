"""Aletheia Python SDK."""

import os
from typing import Optional, Union

from .client import AletheiaAPIError, AletheiaClient

__all__ = [
    "AletheiaAPIError",
    "AletheiaClient",
    "sign",
    "verify",
    "get_evidence",
    "get_evidence_path",
]


def _resolve_base_url(base_url: Optional[str]) -> str:
    if base_url:
        return base_url
    env = os.getenv("ALETHEIA_API_URL")
    if env:
        return env
    raise ValueError("base_url is required (or set ALETHEIA_API_URL)")


def sign(
    response: str,
    model_id: Optional[str] = None,
    policy_id: Optional[str] = None,
    prompt: Optional[str] = None,
    request_id: Optional[str] = None,
    base_url: Optional[str] = None,
    timeout: float = 30.0,
) -> dict:
    client = AletheiaClient(_resolve_base_url(base_url), timeout=timeout)
    return client.sign(
        response=response,
        model_id=model_id,
        policy_id=policy_id,
        prompt=prompt,
        request_id=request_id,
    )


def verify(record_id: Union[int, str], base_url: Optional[str] = None, timeout: float = 30.0) -> dict:
    client = AletheiaClient(_resolve_base_url(base_url), timeout=timeout)
    return client.verify(record_id)


def get_evidence(record_id: Union[int, str], base_url: Optional[str] = None, timeout: float = 30.0) -> bytes:
    client = AletheiaClient(_resolve_base_url(base_url), timeout=timeout)
    return client.get_evidence(record_id)


def get_evidence_path(
    record_id: Union[int, str],
    path: str,
    base_url: Optional[str] = None,
    timeout: float = 30.0,
) -> str:
    client = AletheiaClient(_resolve_base_url(base_url), timeout=timeout)
    return client.get_evidence_path(record_id, path)
