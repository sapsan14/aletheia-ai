import json
from unittest.mock import Mock

import requests

from aletheia.client import AletheiaAPIError, AletheiaClient


def _response(status_code: int, json_body=None, content: bytes = b"") -> requests.Response:
    resp = requests.Response()
    resp.status_code = status_code
    if json_body is not None:
        resp._content = json.dumps(json_body).encode("utf-8")
        resp.headers["Content-Type"] = "application/json"
    else:
        resp._content = content
        resp.headers["Content-Type"] = "application/octet-stream"
    resp.url = "http://localhost"
    return resp


def test_sign_sends_payload_and_returns_json():
    session = Mock()
    session.request.return_value = _response(200, {"id": 1})
    client = AletheiaClient("http://localhost:8080", session=session)

    result = client.sign("hello", model_id="external", policy_id="policy-1")

    assert result["id"] == 1
    session.request.assert_called_once()
    method, url = session.request.call_args.args[:2]
    payload = session.request.call_args.kwargs["json"]
    assert method == "POST"
    assert url == "http://localhost:8080/api/sign"
    assert payload["response"] == "hello"
    assert payload["modelId"] == "external"
    assert payload["policyId"] == "policy-1"


def test_verify_calls_correct_url():
    session = Mock()
    session.request.return_value = _response(200, {"id": 123})
    client = AletheiaClient("http://localhost:8080", session=session)

    result = client.verify(123)

    assert result["id"] == 123
    method, url = session.request.call_args.args[:2]
    assert method == "GET"
    assert url == "http://localhost:8080/api/ai/verify/123"


def test_get_evidence_returns_bytes():
    session = Mock()
    session.request.return_value = _response(200, content=b"zipdata")
    client = AletheiaClient("http://localhost:8080", session=session)

    data = client.get_evidence("abc")

    assert data == b"zipdata"
    method, url = session.request.call_args.args[:2]
    assert method == "GET"
    assert url == "http://localhost:8080/api/ai/evidence/abc"


def test_non_2xx_raises_api_error():
    session = Mock()
    session.request.return_value = _response(400, {"error": "Bad request"})
    client = AletheiaClient("http://localhost:8080", session=session)

    try:
        client.sign("hello")
    except AletheiaAPIError as exc:
        assert exc.status_code == 400
        assert "Bad request" in exc.message
    else:
        raise AssertionError("Expected AletheiaAPIError")
