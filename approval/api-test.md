Approval Framework – End-to-End Postman Testing Guide

Audience: QA / API Testers
Scope: Complete approval framework – setup → runtime approvals → actions → edge cases
Execution Style: Sequential (DO NOT SKIP STEPS)
Environment: Spring Boot 3.5.x, REST APIs

0️⃣ Prerequisites (MANDATORY)
0.1 Required Tools

Postman (latest)

Access to API base URL

Valid tenant ID

At least 2 users:

user1 (approver)

user2 (approver)

One business entity flow (example: GATEPASS)

0.2 Postman Environment Variables

Create a Postman Environment named: approval-local

Variable	Example Value
baseUrl	http://localhost:8080
tenantId	EZY_INFRA_DEV
authTokenUser1	Bearer eyJ...
authTokenUser2	Bearer eyJ...
approvalTemplateId	(empty)
entityId	(empty)
approvalInstanceId	(empty)
1️⃣ Health Check (VERIFY SYSTEM IS UP)
API
GET {{baseUrl}}/actuator/health

Expected Response
{
  "status": "UP"
}


❌ If not UP → STOP testing.

2️⃣ Create Approval Template (ONCE)
Purpose

Defines approval levels & behavior.

API
POST {{baseUrl}}/api/approval/templates

Headers
Authorization: {{authTokenUser1}}
Content-Type: application/json
X-Tenant-Id: {{tenantId}}

Request Body
{
  "name": "Gatepass Approval Template",
  "active": true,
  "levels": [
    {
      "levelOrder": 1,
      "assignmentStrategy": "RUNTIME_MULTI",
      "requireAllApprovals": false,
      "slaDuration": "PT24H",
      "reminderBefore": "PT4H"
    }
  ]
}

Expected Response
{
  "id": "UUID",
  "name": "Gatepass Approval Template"
}

Postman Test Script
pm.environment.set("approvalTemplateId", pm.response.json().id);

3️⃣ Configure Approval Integration (ENTITY → TEMPLATE)
Purpose

Attach approval to entity type.

API
POST {{baseUrl}}/api/approval/integrations

Headers
Authorization: {{authTokenUser1}}
Content-Type: application/json
X-Tenant-Id: {{tenantId}}

Request Body
{
  "entityType": "GATEPASS",
  "approvalTemplateId": "{{approvalTemplateId}}",
  "enabled": true,
  "requireApproverInput": true
}

Expected Response
{
  "entityType": "GATEPASS",
  "enabled": true
}

4️⃣ Create Business Entity (Triggers Approval)

This simulates Gatepass creation

API
POST {{baseUrl}}/api/entries

Headers
Authorization: {{authTokenUser1}}
Content-Type: application/json
X-Tenant-Id: {{tenantId}}

Request Body
{
  "type": "GATEPASS",
  "payload": {
    "requestedBy": "Nikhil",
    "requestedFor": "gyan.patel, rahul.verma",
    "purposeOfVisit": "Delivery",
    "requestingCompany": "Ezyinfra",
    "requestedCompany": "IOCL",
    "requestDateTime": "2026-01-06T09:30:00",
    "isCarryingEquipment": false
  }
}

Expected Response
{
  "id": "UUID",
  "status": "SUCCESS"
}

Postman Script
pm.environment.set("entityId", pm.response.json().id);

5️⃣ Trigger Approval With Runtime Approvers
API
POST {{baseUrl}}/api/approvals/trigger

Headers
Authorization: {{authTokenUser1}}
Content-Type: application/json
X-Tenant-Id: {{tenantId}}

Request Body
{
  "entityType": "GATEPASS",
  "entityId": "{{entityId}}",
  "runtimeApprovers": [
    {
      "levelOrder": 1,
      "users": ["gyan.patel", "rahul.verma"]
    }
  ]
}

Expected Response
{
  "instanceId": "UUID",
  "status": "PENDING",
  "locked": true
}

Postman Script
pm.environment.set("approvalInstanceId", pm.response.json().instanceId);

6️⃣ Fetch Pending Approvals (User-Specific)
API
GET {{baseUrl}}/api/approvals/my-pending

Headers
Authorization: {{authTokenUser1}}
X-Tenant-Id: {{tenantId}}

Expected Response
[
  {
    "instanceId": "{{approvalInstanceId}}",
    "entityType": "GATEPASS",
    "entityId": "{{entityId}}",
    "title": "Level 1 Approval",
    "actions": ["APPROVE", "REJECT"]
  }
]

7️⃣ Approve (User 1)
API
POST {{baseUrl}}/api/approvals/{{approvalInstanceId}}/act

Headers
Authorization: {{authTokenUser1}}
Content-Type: application/json
X-Tenant-Id: {{tenantId}}

Request Body
{
  "approve": true,
  "comment": "Approved by User1"
}

Expected Response
{
  "instanceId": "{{approvalInstanceId}}",
  "status": "APPROVED",
  "locked": false,
  "timeline": [
    {
      "approver": "gyan.patel",
      "status": "APPROVED"
    }
  ]
}

8️⃣ Attempt Double Approval (NEGATIVE TEST)
API

Same as above

Expected Error (RFC-7807)
{
  "type": "approval-already-completed",
  "status": 409,
  "detail": "Approval already completed"
}

9️⃣ Authorization Test (Wrong User)
Headers
Authorization: {{authTokenUser2}}

Expected Error
{
  "type": "approval-unauthorized",
  "status": 403
}

🔟 Rejection Flow (Fresh Instance)

Repeat Steps 4–6, then:

Reject
{
  "approve": false,
  "comment": "Rejecting request"
}

Expected
{
  "status": "REJECTED",
  "locked": false
}

1️⃣1️⃣ SLA / Reminder Verification
API
GET {{baseUrl}}/api/approvals/reminders/due

Expected
[]


(After SLA expiry → entries appear)

1️⃣2️⃣ Timeline Audit API
API
GET {{baseUrl}}/api/approvals/{{approvalInstanceId}}/timeline

Expected
[
  {
    "approver": "gyan.patel",
    "action": "APPROVED",
    "actedAt": "2026-01-05T17:15:30Z"
  }
]

1️⃣3️⃣ Concurrency Test (Optimistic Lock)

Open two Postman tabs

Approve simultaneously

Expected (one fails)
{
  "type": "approval-concurrency",
  "status": 409
}

1️⃣4️⃣ Cleanup (Optional)

Disable integration:

DELETE /api/approval/integrations/GATEPASS

✅ Final Validation Checklist
Item	Expected
Approval tasks created	✅
Entity locked during approval	✅
Only assigned user can approve	✅
Double approval blocked	✅
Rejection short-circuits	✅
DTO-based responses only	✅
RFC-7807 errors	✅
🚨 Tester Rules (IMPORTANT)

DO NOT skip steps

DO NOT reuse approvalInstanceId across tests

ALWAYS reset environment for fresh flows

NEVER expect entities in responses