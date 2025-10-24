# Submissions service

The **submissions** module stores normalized records that conform to template
schemas.  It exposes REST endpoints for creating, retrieving and listing
submissions on a per‑tenant basis.  Every submission belongs to a specific
template type and version; the submission service verifies that the
template exists and uses JSON Schema validation to enforce the structure
【115756199499176†L68-L71】.

## API endpoints

All submission endpoints are nested under `/api/v1/{tenantId}/templates/{type}/{version}`:

| Method & path | Purpose |
|--------------|--------|
| `POST /submissions` | Create a new submission with a normalized payload.  Returns the stored record id and normalized payload. |
| `GET /submissions/{id}` | Retrieve a single submission by id.  Returns the record id, tenantId, template type, version and normalized payload. |
| `GET /submissions` | List all submissions for the given tenant, type and version.  Returns an array of `SubmissionDto` objects.  Pagination is not yet implemented【115756199499176†L92-L93】. |

### Creating a submission

Submit the normalized payload defined by your template.  The payload must
contain all required properties and must not include unknown properties.
The service does not persist the original raw payload; if you wish to
store additional information alongside the normalized data, add optional
fields to your schema.  Example:

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
        "movementId": "move‑007",
        "equipmentId": "Loader‑2",
        "fromSite": "Site‑X",
        "toSite": "Site‑Y",
        "date": "2025‑10‑20T15:00:00Z"
      }' \
  http://localhost:8080/api/v1/sample_tenant/templates/tt_movement_register/1/submissions
```

The response will include the generated record id and echo the normalized
payload.  If validation fails a `VALIDATION_ERROR` response is returned
with details.

### Retrieving a submission

Use the GET endpoint with the record id:

```bash
curl http://localhost:8080/api/v1/sample_tenant/templates/tt_movement_register/1/submissions/42
```

The response includes the `id`, `tenantId`, `type`, `templateVersion` and
`normalized` payload.  The service checks that the type and version in
the URL match those stored on the record; if they do not match a 404
error is returned.

### Listing submissions

List all submissions for a template type and version:

```bash
curl http://localhost:8080/api/v1/sample_tenant/templates/tt_movement_register/1/submissions
```

The response is an array of `SubmissionDto` objects sorted by insertion
order.  Pagination is not currently supported【115756199499176†L92-L93】;
for large result sets you should implement pagination in a future
iteration.

## Submission status lifecycle

Submissions carry a `status` field defined by the `SubmissionStatus` enum.
The statuses are:

| Status | Meaning |
|-------|---------|
| `PENDING` | The submission has been accepted but not yet processed.  This status is available for future asynchronous processing pipelines. |
| `SUCCESS` | The submission has been validated and persisted successfully【115756199499176†L21-L22】. |
| `FAILED`  | The submission failed validation or processing.  Currently failures surface as 400 responses; if asynchronous processing is introduced the status may be updated to FAILED. |

The current implementation persists submissions synchronously and marks
them as `SUCCESS`.  As your workflow evolves you can extend the submission
service to trigger downstream processing, update statuses and record
processing metadata.  The `RecordEntity` also defines optional `payload`
and `processingMeta` fields for storing the raw input and additional
metadata; these are not exposed via the public API for security reasons.
