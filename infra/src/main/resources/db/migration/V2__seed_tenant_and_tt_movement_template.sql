--
-- Seed data for Infraimatic. Inserts a sample tenant template for testing.
--

INSERT INTO template_definitions (tenant_id, type, version, name, json_schema, attribute_refs, created_at, updated_at)
VALUES (
    'sample_tenant',
    'tt_movement_register',
    1,
    'TT Movement Register',
    -- JSON Schema for TT Movement Register template
    $$
    {
      "$schema": "http://json-schema.org/draft-07/schema#",
      "title": "TT Movement Register - normalized schema",
      "type": "object",
      "required": ["movementId","equipmentId","fromSite","toSite","date"],
      "properties": {
        "movementId": {"type":"string"},
        "equipmentId": {"type":"string"},
        "fromSite": {"type":"string"},
        "toSite": {"type":"string"},
        "movedBy": {"type":"string"},
        "reason": {"type":"string"},
        "date": {"type":"string","format":"date-time"}
      },
      "additionalProperties": false
    }
    $$::jsonb,
    NULL,
    NOW(),
    NOW()
);