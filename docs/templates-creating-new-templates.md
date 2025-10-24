# Creating and managing custom templates

While a default movement register template is provided, Infraimatic is
designed to support **arbitrary template types**.  A template defines the
canonical JSON structure that normalized submissions must follow.  By
registering a new template you can instruct the submission service to
validate incoming records against your domain‑specific schema.  This section
explains how to create custom templates and provides a worked example.

## Steps to create a template

1. **Design the JSON Schema.**  Use [JSON Schema Draft 7](https://json-schema.org/) to
   describe your normalized payload.  Include `type`, `required`, `properties`
   and `additionalProperties` keys.  Infraimatic stores the schema as JSONB
   and uses the networknt validator to enforce it【115756199499176†L68-L71】.
2. **Choose a template type.**  The `type` identifies your template (e.g.
   `tt_equipment_failure_report`).  It should start with a prefix (such as
   `tt_`) to distinguish template types from other entities.
3. **POST to `/api/v1/{tenantId}/templates/{type}`** with a `TemplateCreateRequest`.
   Include a descriptive `name` and your `jsonSchema` as a JSON object.
   The service will assign the next version number automatically.
4. **Test the schema by creating submissions.**  Use
   `POST /api/v1/{tenantId}/templates/{type}/{version}/submissions` to submit
   normalized payloads.  Validation errors return HTTP 400 with details.
5. **Iterate on the schema.**  When you need to change the structure create
   a new template version; submissions remain tied to the version used at
   creation time.

### Sample template: equipment failure report

Assume you operate heavy machinery and need to record equipment failures.  You
decide to capture details such as the failure identifier, equipment ID, site,
severity, description, reporter and date.  The following JSON Schema (Draft 7)
expresses these requirements.  Save this schema in a JSON file or embed it
directly in the request body.

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "title": "Equipment Failure Report - normalized schema",
  "type": "object",
  "required": ["failureId", "equipmentId", "site", "severity", "description", "date"],
  "properties": {
    "failureId": {"type": "string"},
    "equipmentId": {"type": "string"},
    "site": {"type": "string"},
    "severity": {"type": "string", "enum": ["Low", "Medium", "High", "Critical"]},
    "description": {"type": "string"},
    "reportedBy": {"type": "string"},
    "date": {"type": "string", "format": "date-time"}
  },
  "additionalProperties": false
}
```

To create this template for tenant `sample_tenant` run:

```bash
curl -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{
           "name": "Equipment Failure Report",
           "jsonSchema": { /* schema above */ }
         }' \
     http://localhost:8080/api/v1/sample_tenant/templates/tt_equipment_failure_report
```

The response will include the assigned version (e.g. `1`).  You can now
submit normalized failure reports.

### Submission examples

The following examples illustrate how to submit normalized payloads that
conform to the `tt_equipment_failure_report` schema.  Replace `<version>`
with the version returned from the create template call.  These examples use
the fields defined above and demonstrate both valid and invalid scenarios.

#### Example 1 – all required fields

```bash
curl -X POST \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
        "failureId": "fail‑001",
        "equipmentId": "Generator‑12",
        "site": "Plant‑A",
        "severity": "High",
        "description": "Oil leakage detected",
        "date": "2025‑10‑02T08:15:00Z"
      }' \
  http://localhost:8080/api/v1/sample_tenant/templates/tt_equipment_failure_report/<version>/submissions
```

#### Example 2 – including optional `reportedBy`

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
        "failureId": "fail‑002",
        "equipmentId": "Motor‑7",
        "site": "Site‑B",
        "severity": "Medium",
        "description": "Unusual vibration",
        "reportedBy": "Jane Doe",
        "date": "2025‑10‑05T14:45:00Z"
      }' \
  http://localhost:8080/api/v1/sample_tenant/templates/tt_equipment_failure_report/<version>/submissions
```

#### Example 3 – invalid severity (enumeration violation)

The `severity` property must be one of `Low`, `Medium`, `High` or `Critical`.
Using another value produces a validation error.

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
        "failureId": "fail‑003",
        "equipmentId": "Compressor‑9",
        "site": "Plant‑B",
        "severity": "Severe",  /* invalid */
        "description": "Loud noise",
        "date": "2025‑10‑10T09:00:00Z"
      }' \
  http://localhost:8080/api/v1/sample_tenant/templates/tt_equipment_failure_report/<version>/submissions
```

#### Example 4 – missing required field

Omitting a required property such as `description` triggers a 400 response with
detailed validation errors.

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
        "failureId": "fail‑004",
        "equipmentId": "Pump‑2",
        "site": "Plant‑C",
        "severity": "Low",
        "date": "2025‑10‑12T11:20:00Z"
      }' \
  http://localhost:8080/api/v1/sample_tenant/templates/tt_equipment_failure_report/<version>/submissions
```

#### Example 5 – additional property

Because `additionalProperties` is set to false, extra keys such as
`unexpected` will cause validation to fail.

```bash
curl -X POST \
  -H "Content-Type: application/json" \
  -d '{
        "failureId": "fail‑005",
        "equipmentId": "Motor‑3",
        "site": "Site‑D",
        "severity": "Critical",
        "description": "Bearing failure",
        "unexpected": "not allowed",
        "date": "2025‑10‑15T13:00:00Z"
      }' \
  http://localhost:8080/api/v1/sample_tenant/templates/tt_equipment_failure_report/<version>/submissions
```

### Versioning and evolution

Templates are versioned.  To evolve the schema (e.g. adding a new required
field or relaxing constraints) POST a new definition to the same
`/templates/{type}` endpoint.  Existing submissions remain linked to their
original version and are not re‑validated.  Use the version in the URL when
submitting data to specify which version your payload targets.
