# Natural Language Understanding (NLU)

Infraimatic includes a **pluggable NLU module** that can transform free‑form
text into structured submissions.  This module exposes two REST endpoints:
`/nlu/parse` returns a recommended template and normalized payload, and
`/nlu/submit` persists the parsed submission directly.  The default
implementation uses a simple regular expression to extract fields for the
movement register template; you can plug in your own model by implementing
the `EnrichmentService` interface.

## Endpoints

| Method & path | Purpose |
|--------------|--------|
| `POST /api/v1/{tenantId}/nlu/parse` | Parses free text and returns the template type, version, normalized payload and a confidence score. |
| `POST /api/v1/{tenantId}/nlu/submit` | Parses text, persists a new submission using the recommended template and returns the submission identifier and normalized payload. |

### Parse endpoint

Request body (JSON):

```json
{
  "text": "<free‑form sentence>"
}
```

Response (HTTP 200):

```json
{
  "templateType": "tt_movement_register",
  "templateVersion": 1,
  "normalized": { /* extracted fields */ },
  "confidence": 0.85
}
```

The default parser always returns `tt_movement_register` and extracts
`equipmentId`, `fromSite`, `toSite` and `date` from sentences matching the
pattern `<equipment> moved from <from> to <to> ... at <date>`【115756199499176†L85-L87】.

### Submit endpoint

The submit endpoint behaves like the parse endpoint but additionally calls
the submission service to persist the normalized payload【966698866192483†L132-L158】.  If the
payload does not satisfy the schema of the recommended template, a
validation error is returned.  For example:

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{"text":"Pump‑3 moved from Site‑A to Site‑B at 2025‑09‑28T10:20:00Z"}' \
  http://localhost:8080/api/v1/sample_tenant/nlu/submit
```

**Response:**

```json
{
  "submissionId": 101,
  "normalized": {
    "equipmentId": "Pump‑3",
    "fromSite": "Site‑A",
    "toSite": "Site‑B",
    "date": "2025‑09‑28T10:20:00Z"
  }
}
```

## Extending the NLU module

The class `EnrichmentServiceImpl` provides the default implementation.  To
integrate a more sophisticated parser—such as an LLM or an external NLP
service—create a new class implementing `EnrichmentService` and annotate it
with `@Service`.  The interface defines two methods:

* `NluParseResponse parse(String tenantId, String text)` – returns the
  recommended template type/version, normalized payload and confidence.
* `NluSubmitResponse submit(String tenantId, String text)` – parses the text,
  persists the resulting submission via `SubmissionService` and returns the
  identifier.

You can inject additional components (e.g., template predictors, entity
extractors) into your service.  If multiple `EnrichmentService` beans are
present Spring will require a qualifier; alternatively, mark your bean as
`@Primary` to override the default one.
